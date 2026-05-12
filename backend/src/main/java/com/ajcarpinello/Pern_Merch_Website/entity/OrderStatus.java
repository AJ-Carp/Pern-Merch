package com.ajcarpinello.Pern_Merch_Website.entity;

public enum OrderStatus {
    PENDING_PAYMENT,   // order created, awaiting payment
    PAID,              // payment_intent.succeeded received
    CONFIRMED,         // admin-acknowledged
    SHIPPED,
    DELIVERED,
    CANCELLED          // payment failed/refunded
}