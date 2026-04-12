package com.ajcarpinello.Pern_Merch_Website.controller;

import com.ajcarpinello.Pern_Merch_Website.dto.AddToCartRequest;
import com.ajcarpinello.Pern_Merch_Website.dto.CartItemDTO;
import com.ajcarpinello.Pern_Merch_Website.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // @AuthenticationPrincipal automatically injects the details of the currently logged-in user
    @GetMapping
    public ResponseEntity<List<CartItemDTO>> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(cartService.getCart(userDetails.getUsername()));
    }

    @PostMapping
    public ResponseEntity<CartItemDTO> addToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody AddToCartRequest request) {
        return ResponseEntity.ok(cartService.addToCart(userDetails.getUsername(), request.getProductId(), request.getQuantity()));
    }

    @PutMapping("/{cartItemId}")
    public ResponseEntity<CartItemDTO> updateQuantity(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long cartItemId,
            // intercepting JSON input as a map, so we can just get the quantity value without a dto
            @RequestBody Map<String, Integer> body) {
        CartItemDTO updated = cartService.updateQuantity(
                userDetails.getUsername(), cartItemId, body.get("quantity")
        );
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<Void> removeFromCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long cartItemId) {
        cartService.removeFromCart(userDetails.getUsername(), cartItemId);
        return ResponseEntity.noContent().build();
    }
}
