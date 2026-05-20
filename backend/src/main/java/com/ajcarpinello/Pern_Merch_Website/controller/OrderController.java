package com.ajcarpinello.Pern_Merch_Website.controller;

import com.ajcarpinello.Pern_Merch_Website.dto.OrderDTO;
import com.ajcarpinello.Pern_Merch_Website.dto.PaymentIntentResponse;
import com.ajcarpinello.Pern_Merch_Website.service.OrderService;
import com.ajcarpinello.Pern_Merch_Website.service.PaymentService;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final PaymentService paymentService;

    @PostMapping("/checkout")
    public ResponseEntity<PaymentIntentResponse> checkout(@AuthenticationPrincipal UserDetails user) throws StripeException {
        return ResponseEntity.ok(paymentService.initiateCheckout(user.getUsername()));
    }

    @PostMapping("/cancel")
    public ResponseEntity<Void> cancelPendingCheckout(@AuthenticationPrincipal UserDetails user) throws StripeException {
        PaymentService.CancelOutcome outcome = paymentService.cancelPendingCheckout(user.getUsername());
        if (outcome == PaymentService.CancelOutcome.ALREADY_PAID) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); // payment already completed
        }
        return ResponseEntity.ok().build(); // CANCELLED or NOOP — idempotent success
    }

    @GetMapping
    public ResponseEntity<List<OrderDTO>> getOrderHistory(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(orderService.getOrderHistory(userDetails.getUsername()));
    }
}
