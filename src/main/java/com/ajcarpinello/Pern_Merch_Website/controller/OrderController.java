package com.ajcarpinello.Pern_Merch_Website.controller;

import com.ajcarpinello.Pern_Merch_Website.dto.OrderDTO;
import com.ajcarpinello.Pern_Merch_Website.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    public ResponseEntity<OrderDTO> checkout(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(orderService.checkout(userDetails.getUsername()));
    }

    @GetMapping
    public ResponseEntity<List<OrderDTO>> getOrderHistory(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(orderService.getOrderHistory(userDetails.getUsername()));
    }
}
