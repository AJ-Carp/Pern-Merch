package com.ajcarpinello.Pern_Merch_Website.repository;

import com.ajcarpinello.Pern_Merch_Website.entity.Order;
import com.ajcarpinello.Pern_Merch_Website.entity.OrderStatus;
import com.ajcarpinello.Pern_Merch_Website.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    
    List<Order> findByUserOrderByOrderDateDesc(User user);

    Optional<Order> findByStripePaymentIntentId(String stripePaymentIntentId);

    Optional<Order> findFirstByUserAndStatusOrderByOrderDateDesc(User user, OrderStatus status);
}
