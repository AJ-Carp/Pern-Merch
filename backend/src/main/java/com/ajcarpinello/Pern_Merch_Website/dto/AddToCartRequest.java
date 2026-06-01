package com.ajcarpinello.Pern_Merch_Website.dto;

import lombok.Data;

@Data
public class AddToCartRequest {
    private Long productVariantId;
    private int quantity;
}
