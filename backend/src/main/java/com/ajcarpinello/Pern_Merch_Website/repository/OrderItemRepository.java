package com.ajcarpinello.Pern_Merch_Website.repository;

import com.ajcarpinello.Pern_Merch_Website.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    boolean existsByVariantId(Long variantId);
}
