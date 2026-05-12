package com.ajcarpinello.Pern_Merch_Website.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // LAZY - user who placed order is not loaded from DB until its used
    // user_id is foreign key in orders and primary key in users
    // there is no column of user objects, it's the user_id column
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime orderDate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(unique = true)
    private String stripePaymentIntentId;

    private LocalDateTime paidAt;


    // cascade - if we save a new order, the associated orderItems will also be saved
    // mappedBy references the foreign key in OrderItem. That foreign key references the order it belongs to
    /* orphanRemoval - if you remove an OrderItem from the items list of an Order and then save the Order,
       that OrderItem will also be deleted from the database */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();
}