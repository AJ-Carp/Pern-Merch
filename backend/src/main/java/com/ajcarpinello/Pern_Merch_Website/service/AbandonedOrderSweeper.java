package com.ajcarpinello.Pern_Merch_Website.service;

import com.ajcarpinello.Pern_Merch_Website.entity.Order;
import com.ajcarpinello.Pern_Merch_Website.entity.OrderStatus;
import com.ajcarpinello.Pern_Merch_Website.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Releases stock and cancels the Stripe PaymentIntent for checkouts the customer
 * started but never paid. This is the only terminal-cleanup path for abandoned
 * orders: Stripe never auto-cancels a standalone PaymentIntent, and there is no
 * webhook for "customer walked away".
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AbandonedOrderSweeper {

    private final OrderRepository orderRepository;
    private final PaymentService paymentService;

    /** Pending orders older than this many minutes are reclaimed. Tunable via config. */
    @Value("${checkout.expiry-minutes:30}")
    private long expiryMinutes;

    @Scheduled(fixedDelayString = "${checkout.sweep-interval-ms:60000}")
    public void sweepAbandonedOrders() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(expiryMinutes);
        List<Order> stale = orderRepository.findByStatusAndOrderDateBefore(OrderStatus.PENDING_PAYMENT, cutoff);
        if (stale.isEmpty()) return;

        log.info("Sweeping {} abandoned pending order(s) older than {} min", stale.size(), expiryMinutes);
        for (Order order : stale) {
            try {
                PaymentService.CancelOutcome outcome = paymentService.cancelOrderFully(order.getId());
                log.info("Swept order {} -> {}", order.getId(), outcome);
            } catch (Exception e) {
                // One bad order (PI still processing, Stripe unreachable, etc.) must
                // not abort the batch. It stays PENDING_PAYMENT and is retried next sweep.
                log.warn("Failed to sweep order {}: {}", order.getId(), e.getMessage());
            }
        }
    }
}
