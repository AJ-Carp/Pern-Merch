package com.ajcarpinello.Pern_Merch_Website.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String productImageUrl;
    private BigDecimal productPrice;
    private String size;
    private int quantity;
}
