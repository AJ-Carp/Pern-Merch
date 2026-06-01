package com.ajcarpinello.Pern_Merch_Website.entity;

/*
    cart_items is a temporary shopping cart it holds
    products a user is considering buying before they check
    out. Once they place an order, those cart items get
    converted into order_items (the permanent purchase record)
    and the cart is cleared.
 */

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cart_items")
@Builder
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @Column(nullable = false)
    private int quantity;
}
