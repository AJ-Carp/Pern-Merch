package com.ajcarpinello.Pern_Merch_Website.service;

import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import com.ajcarpinello.Pern_Merch_Website.dto.PaymentIntentResponse;
import com.ajcarpinello.Pern_Merch_Website.entity.Order;
import com.ajcarpinello.Pern_Merch_Website.entity.OrderStatus;
import com.ajcarpinello.Pern_Merch_Website.entity.ProcessedStripeEvent;
import com.ajcarpinello.Pern_Merch_Website.repository.OrderRepository;
import com.ajcarpinello.Pern_Merch_Website.repository.ProcessedStripeEventRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final StripeService stripeService;
    private final ProcessedStripeEventRepository eventRepository;

    public PaymentIntentResponse createPaymentIntent(String username) throws StripeException {
        // Tx 1 (in OrderService): reserve stock + create or reuse pending order. Short.
        Order order = orderService.createPendingOrder(username);

        // If this is a reused order that already has an intent, verify it is still usable
        if (order.getStripePaymentIntentId() != null) {
            PaymentIntent existing = stripeService.retrievePaymentIntent(order.getStripePaymentIntentId());
            if ("canceled".equals(existing.getStatus())) {
                orderService.cancelAndReleaseStock(order.getId());
                order = orderService.createPendingOrder(username);
            } else {
                return new PaymentIntentResponse(existing.getClientSecret(), order.getId());
            }
        }

        // Network call to Stripe — NO transaction active, NO DB locks held
        PaymentIntent intent;
        try {
            intent = stripeService.createPaymentIntent(order);
        } catch (StripeException e) {
            // Compensating transaction: release stock, cancel order
            orderService.cancelAndReleaseStock(order.getId());
            throw e;
        }

        // Tx 2 (in OrderService): persist the intent ID
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

    @Transactional
    public void handlePaymentSucceeded(Event event) {
        PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElseThrow();
        Order order = orderRepository.findByStripePaymentIntentId(intent.getId())
                .orElseThrow(() -> new IllegalStateException("Order not found for PI: " + intent.getId()));

        if (order.getStatus() == OrderStatus.PAID) return; // order-level idempotency

        order.setStatus(OrderStatus.PAID);
        order.setPaidAt(LocalDateTime.now());
        orderRepository.save(order);
        
        cartService.clearCart(order.getUser().getUsername());
    }

    @Transactional
    public void handlePaymentFailed(Event event) {
        PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElseThrow();
        Order order = orderRepository.findByStripePaymentIntentId(intent.getId()).orElseThrow();
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) return;
        orderService.cancelAndReleaseStock(order.getId());
    }
}