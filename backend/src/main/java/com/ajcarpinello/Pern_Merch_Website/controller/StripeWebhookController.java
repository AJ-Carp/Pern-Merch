package com.ajcarpinello.Pern_Merch_Website.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ajcarpinello.Pern_Merch_Website.service.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/webhooks/stripe")
@RequiredArgsConstructor
public class StripeWebhookController {

    @Value("${stripe.webhook-secret}") private String webhookSecret;
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<String> handle(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {

        Event event;
        try {
            event = Webhook.constructEvent(payload, signature, webhookSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.status(400).body("Invalid signature");
        }

        // stripe will keep sending the webhook over again unitl it gets a 200 or 400
        if (paymentService.isEventProcessed(event.getId())) {
            return ResponseEntity.ok("Already processed");
        }

        try {
            switch (event.getType()) {
                case "payment_intent.succeeded" -> paymentService.handlePaymentSucceeded(event);
                case "payment_intent.payment_failed", "payment_intent.canceled"
                    -> paymentService.handlePaymentFailed(event);
                default -> { /* ignore */ }
            }
            paymentService.markEventProcessed(event.getId(), event.getType());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Processing failed");
        }
        return ResponseEntity.ok("ok");
    }
}