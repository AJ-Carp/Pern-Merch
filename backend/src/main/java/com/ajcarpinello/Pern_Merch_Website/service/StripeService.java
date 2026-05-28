package com.ajcarpinello.Pern_Merch_Website.service;

import org.springframework.stereotype.Service;
import com.ajcarpinello.Pern_Merch_Website.entity.Order;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

@Service
@Slf4j
public class StripeService {

    @Value("${stripe.secret-key}") private String secretKey;

    @PostConstruct
    public void init() { Stripe.apiKey = secretKey; }

    public PaymentIntent createPaymentIntent(Order order) throws StripeException {
        long amountInCents = order.getTotalAmount()
                .multiply(BigDecimal.valueOf(100)).longValueExact();

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency("usd")
                .setAutomaticPaymentMethods(
                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                        .setEnabled(true).build())
                .putMetadata("orderId", order.getId().toString())
                .setDescription("Order #" + order.getId())
                // could change to this, but user must not be loaded lazily
                // .setDescription("Order Id: " + order.getId() + " | Customer username: " + 
                //     order.getUser().getUsername() + " | Customer email: " + order.getUser().getEmail() +
                //     " | Order items: " + order.getItems())
                .build();

        // Idempotency key — Stripe returns the same intent if called twice with this key
        RequestOptions opts = RequestOptions.builder()
                .setIdempotencyKey("order-" + order.getId())
                .build();

        return PaymentIntent.create(params, opts);
    }

    // stripe stores all paymentIntents made and we can fetch them via id
    public PaymentIntent retrievePaymentIntent(String id) throws StripeException {
        return PaymentIntent.retrieve(id);
    }

    public PaymentIntent cancelPaymentIntent(String intentId) throws StripeException {
        PaymentIntent pi = PaymentIntent.retrieve(intentId);
        String s = pi.getStatus();
        if ("succeeded".equals(s) || "canceled".equals(s)) return pi; // terminal — caller inspects status
        log.warn("Cancelling PaymentIntent {} (current status: {}) -- stack trace follows", intentId, s, new Throwable("PI cancel call site"));
        return pi.cancel();
    }
}