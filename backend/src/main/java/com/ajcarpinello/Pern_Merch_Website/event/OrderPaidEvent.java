package com.ajcarpinello.Pern_Merch_Website.event;

import com.ajcarpinello.Pern_Merch_Website.entity.Address;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.math.BigDecimal;
import java.util.List;

/**
 * Published by OrderService.finalizeOrder once an order is marked PAID. Carries a full
 * snapshot (no JPA entities) so the AFTER_COMMIT listener can build emails without touching
 * lazy associations after the transaction has closed.
 */
@Getter
@AllArgsConstructor
public class OrderPaidEvent {
    private Long orderId;
    private String customerEmail;
    private BigDecimal totalAmount;
    private List<Line> items;
    private Address shippingAddress;

    @Getter
    @AllArgsConstructor
    public static class Line {
        private String productName; 
        private String size; 
        private int quantity;
        private BigDecimal price;
    }
}
