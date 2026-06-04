package com.ajcarpinello.Pern_Merch_Website.event;

import com.ajcarpinello.Pern_Merch_Website.entity.Address;
import java.math.BigDecimal;
import java.util.List;

/**
 * Published by OrderService.finalizeOrder once an order is marked PAID. Carries a full
 * snapshot (no JPA entities) so the AFTER_COMMIT listener can build emails without touching
 * lazy associations after the transaction has closed.
 */
public record OrderPaidEvent(
    Long orderId,
    String customerEmail,
    BigDecimal totalAmount,
    List<Line> items,
    Address shippingAddress) {

    public record Line(
        String productName, 
        String size, 
        int quantity, 
        BigDecimal price
    ) {}
}
