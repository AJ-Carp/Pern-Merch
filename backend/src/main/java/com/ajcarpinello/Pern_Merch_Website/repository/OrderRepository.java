package com.ajcarpinello.Pern_Merch_Website.repository;

import com.ajcarpinello.Pern_Merch_Website.entity.Order;
import com.ajcarpinello.Pern_Merch_Website.entity.OrderStatus;
import com.ajcarpinello.Pern_Merch_Website.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserOrderByOrderDateDesc(User user);

    Optional<Order> findByStripePaymentIntentId(String stripePaymentIntentId);

    // PESSIMISTIC_WRITE so a webhook and the sweeper/cancel path can't both pass the
    // PENDING_PAYMENT guard and finalize the same order twice (double cart-clear / double email).
    // The second caller blocks here, then sees PAID and bails on the idempotency guard.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.stripePaymentIntentId = :pi")
    Optional<Order> findByStripePaymentIntentIdForUpdate(@Param("pi") String pi);

    Optional<Order> findFirstByUserAndStatusOrderByOrderDateDesc(User user, OrderStatus status);

    List<Order> findByStatusAndOrderDateBefore(OrderStatus status, LocalDateTime cutoff);
}
