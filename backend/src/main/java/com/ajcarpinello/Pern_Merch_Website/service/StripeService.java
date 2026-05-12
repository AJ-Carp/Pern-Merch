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
import org.springframework.beans.factory.annotation.Value;

@Service
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
                .build();

        // Idempotency key — Stripe returns the same intent if called twice with this key
        RequestOptions opts = RequestOptions.builder()
                .setIdempotencyKey("order-" + order.getId())
                .build();

        return PaymentIntent.create(params, opts);
    }

    public PaymentIntent retrievePaymentIntent(String id) throws StripeException {
        return PaymentIntent.retrieve(id);
    }
}