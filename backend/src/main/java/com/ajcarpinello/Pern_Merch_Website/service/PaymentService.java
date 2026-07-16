package com.ajcarpinello.Pern_Merch_Website.service;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Service;
import com.ajcarpinello.Pern_Merch_Website.dto.PaymentIntentResponse;
import com.ajcarpinello.Pern_Merch_Website.entity.Order;
import com.ajcarpinello.Pern_Merch_Website.entity.OrderStatus;
import com.ajcarpinello.Pern_Merch_Website.entity.ProcessedStripeEvent;
import com.ajcarpinello.Pern_Merch_Website.repository.OrderRepository;
import com.ajcarpinello.Pern_Merch_Website.repository.ProcessedStripeEventRepository;
import com.stripe.Stripe;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final StripeService stripeService;
    private final ProcessedStripeEventRepository eventRepository;

    public PaymentIntentResponse initiateCheckout(String username) throws StripeException {

        Order order;
        Optional<Order> existing = orderService.findPendingOrder(username);
        if (existing.isPresent() && !orderService.pendingOrderCartMatches(existing.get().getId())) {
            cancelOrderFully(existing.get().getId());   // Stripe PI cancel + stock release
            order = orderService.createPendingOrder(username);
        } else if (existing.isPresent()) {
            // existing matches
            order = existing.get();
        } else {
            order = orderService.createPendingOrder(username);
        }

        // If this is a reused order that already has an intent, verify it is still usable
        if (order.getStripePaymentIntentId() != null) {
            PaymentIntent existingPaymentIntent = stripeService.retrievePaymentIntent(order.getStripePaymentIntentId());
            if ("canceled".equals(existingPaymentIntent.getStatus())) {
                orderService.cancelAndReleaseStock(order.getId());
                order = orderService.createPendingOrder(username);
            } else {
                return new PaymentIntentResponse(existingPaymentIntent.getClientSecret(), order.getId());
            }
        }

        PaymentIntent intent;
        try {
            intent = stripeService.createPaymentIntent(order);
        } catch (StripeException e) {
            // Compensating transaction: release stock, cancel order
            orderService.cancelAndReleaseStock(order.getId());
            throw e;
        }

        // Transaction 2 (in OrderService): persist the intent ID
        orderService.attachPaymentIntent(order.getId(), intent.getId());
        return new PaymentIntentResponse(intent.getClientSecret(), order.getId());
    }

    public boolean isEventProcessed(String eventId) {
        return eventRepository.existsById(eventId);
    }

    public void markEventProcessed(String eventId, String eventType) {
        eventRepository.save(ProcessedStripeEvent.builder()
            .eventId(eventId).eventType(eventType).processedAt(LocalDateTime.now()).build());
    }

    public void handlePaymentSucceeded(Event event) {
        PaymentIntent intent = extractIntent(event);
        orderService.finalizeOrder(intent);
    }

    public void handlePaymentFailed(Event event) {
        PaymentIntent intent = extractIntent(event);
        // A failed attempt is NOT terminal — the PaymentIntent stays alive and the
        // customer can retry with another card. Do nothing here; the abandoned-order
        // sweeper / user cancel handles stock release if the order is never paid.
        log.info("{} for PI {} (current status: {}) — no action taken", event.getType(), intent.getId(), intent.getStatus());
    }

    // two version of stripe: API (sending webhook) and our projects version in POM
    // we first try to deserializer the event the safe way (ensures versions match)
    // if it fails, we deserializer using unsafe way and log a warning (events json may not be what we expect)
    private PaymentIntent extractIntent(Event event) {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        if (deserializer.getObject().isPresent()) {
            return (PaymentIntent) deserializer.getObject().get();
        }
        // Safe deserialize failed → account/endpoint API version differs from stripe-java's
        // (2026-03-25.dahlia). Force it; the fields we use (id, status, shipping) are stable.
        log.warn("Event {} arrived on API version {} (lib expects {}); using deserializeUnsafe()",
                event.getId(), event.getApiVersion(), Stripe.API_VERSION);
        try {
            return (PaymentIntent) deserializer.deserializeUnsafe();
        } catch (EventDataObjectDeserializationException e) {
            throw new IllegalStateException("Cannot deserialize event " + event.getId(), e);
        }
    }

    /**
     * Cancels an order end-to-end: the Stripe PaymentIntent first, then the local
     * stock/DB. Stripe-first so we never release stock for a payment that actually
     * went through. Shared by the user cancel button and the abandoned-order sweeper.
     *
     * Deliberately NOT @Transactional: it makes a Stripe network call, which must
     * not hold a DB transaction open. The DB write is delegated to
     * orderService.cancelAndReleaseStock, which has its own short transaction.
     */
    public CancelOutcome cancelOrderFully(Long orderId) throws StripeException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("Order not found: " + orderId));

        // Idempotent: only a still-pending order can be canceled. Covers double
        // clicks and sweeper-vs-button races.
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) return CancelOutcome.NOOP;

        String intentId = order.getStripePaymentIntentId();
        if (intentId != null) {
            PaymentIntent pi = stripeService.cancelPaymentIntent(intentId);
            if ("succeeded".equals(pi.getStatus())) {
                // Payment already went through — finalize it now instead of waiting on the webhook.
                orderService.finalizeOrder(pi);
                return CancelOutcome.ALREADY_PAID;
            }
        }

        orderService.cancelAndReleaseStock(orderId);
        return CancelOutcome.CANCELLED;
    }

    public CancelOutcome cancelPendingCheckout(String username) throws StripeException {
        Optional<Order> pending = orderService.findPendingOrder(username);
        if (pending.isEmpty()) return CancelOutcome.NOOP;
        return cancelOrderFully(pending.get().getId());
    }

    public enum CancelOutcome {
        CANCELLED,     // order canceled, stock released
        ALREADY_PAID,  // payment already succeeded — left for the success webhook
        NOOP           // nothing to do (already canceled/paid/not pending)
    }
}