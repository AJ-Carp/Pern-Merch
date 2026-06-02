package com.ajcarpinello.Pern_Merch_Website.repository;

import com.ajcarpinello.Pern_Merch_Website.entity.CartItem;
import com.ajcarpinello.Pern_Merch_Website.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(User user);
    Optional<CartItem> findByUserAndVariantId(User user, Long variantId);
    boolean existsByVariantId(Long variantId);
    void deleteByUser(User user);
}
