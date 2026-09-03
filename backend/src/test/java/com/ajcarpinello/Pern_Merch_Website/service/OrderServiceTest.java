package com.ajcarpinello.Pern_Merch_Website.service;

import com.ajcarpinello.Pern_Merch_Website.dto.OrderDTO;
import com.ajcarpinello.Pern_Merch_Website.entity.Order;
import com.ajcarpinello.Pern_Merch_Website.entity.OrderStatus;
import com.ajcarpinello.Pern_Merch_Website.exception.AppException;
import com.ajcarpinello.Pern_Merch_Website.repository.CartItemRepository;
import com.ajcarpinello.Pern_Merch_Website.repository.OrderRepository;
import com.ajcarpinello.Pern_Merch_Website.repository.ProductVariantRepository;
import com.ajcarpinello.Pern_Merch_Website.repository.UserRepository;
import com.stripe.model.PaymentIntent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private CartService cartService;
    @Mock private UserRepository userRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private ProductVariantRepository variantRepository;
    @Mock private UserService userService;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private OrderService orderService;

    /**
     * The admin status endpoint binds a bare enum off the request body, so the service
     * guard is the only thing stopping a client from setting any status it likes. The
     * dangerous one is CANCELLED: it would flip the order without ever running
     * cancelAndReleaseStock, stranding the reserved stock permanently.
     */
    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = {"CONFIRMED", "SHIPPED"}, mode = EnumSource.Mode.EXCLUDE)
    void updateOrderStatus_rejectsAnyStatusOffTheFulfillmentLadder(OrderStatus illegal) {
        AppException ex = assertThrows(AppException.class,
                () -> orderService.updateOrderStatus(1L, illegal));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        // The guard must reject before the order is even loaded, let alone saved.
        verifyNoInteractions(orderRepository);
    }

    /** The other half of the guard: the two legal transitions still go through. */
    @Test
    void updateOrderStatus_advancesAConfirmedOrderToShipped() {
        Order order = Order.builder()
                .id(7L)
                .status(OrderStatus.CONFIRMED)
                .orderDate(LocalDateTime.now())
                .totalAmount(new BigDecimal("40.00"))
                .build();
        when(orderRepository.findById(7L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        OrderDTO dto = orderService.updateOrderStatus(7L, OrderStatus.SHIPPED);

        assertEquals(OrderStatus.SHIPPED, order.getStatus());
        assertEquals("SHIPPED", dto.getStatus());
    }

    /**
     * Stripe retries a webhook until it gets a 2xx, and the sweeper and the user's
     * cancel button can both reach finalizeOrder for the same intent. The row lock
     * serializes them; this PENDING_PAYMENT guard is what makes the losers no-ops.
     * Without it the customer gets a second confirmation email and their live cart
     * is cleared a second time.
     */
    @Test
    void finalizeOrder_isANoOpForAnOrderThatIsAlreadyPaid() {
        PaymentIntent intent = new PaymentIntent();
        intent.setId("pi_123");

        Order alreadyPaid = Order.builder().id(3L).status(OrderStatus.PAID).build();
        when(orderRepository.findByStripePaymentIntentIdForUpdate("pi_123"))
                .thenReturn(Optional.of(alreadyPaid));

        orderService.finalizeOrder(intent);

        assertEquals(OrderStatus.PAID, alreadyPaid.getStatus());
        verify(orderRepository, never()).save(any());
        // No second cart clear, and — the expensive mistake — no second email.
        verifyNoInteractions(cartService, eventPublisher);
    }
}
