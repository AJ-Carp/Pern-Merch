package com.ajcarpinello.Pern_Merch_Website.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    /* precision = 10 means the total number of digits allowed in the number
       (both before and after the decimal point). */
    // scale = 2 means the number of digits allowed after the decimal point.
    @Column(nullable = false, precision = 10, scale = 2)
    // big decimal is good when we need to be precise, avoids rounding errors
    private BigDecimal price;

    @Column(nullable = false)
    private String category; // T-Shirts, Hoodies, Accessories, etc.

    private String size; // S, M, L, XL, ONE_SIZE

    private String imageUrl;

    @Column(nullable = false)
    private int stockQuantity;
}
