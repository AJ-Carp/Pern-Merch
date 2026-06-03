package com.ajcarpinello.Pern_Merch_Website.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.math.BigDecimal;
import java.util.List;
import org.hibernate.annotations.ColumnDefault;

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

    private String imageUrl;

    /* Soft-delete flag. "Deleting" a product sets this to false instead of removing the
    row, so its variants stay valid FK targets for past order_items (no history corruption).
    Storefront reads filter to active=true; archived products disappear from the shop. */
    @Column(nullable = false)
    // sets column to true by default in DB
    @ColumnDefault("true")
    // building a product (using builder) will set active to true by default
    @Builder.Default
    private boolean active = true;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    // skip the variants field when generating toString() and equals()/hashCode()
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ProductVariant> variants;
}
