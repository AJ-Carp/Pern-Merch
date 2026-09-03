package com.ajcarpinello.Pern_Merch_Website.service;

import com.ajcarpinello.Pern_Merch_Website.entity.Order;
import com.ajcarpinello.Pern_Merch_Website.entity.OrderStatus;
import com.ajcarpinello.Pern_Merch_Website.repository.OrderRepository;
import com.ajcarpinello.Pern_Merch_Website.repository.ProcessedStripeEventRepository;
import com.stripe.exception.ApiConnectionException;
import com.stripe.exception.StripeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private OrderService orderService;
    @Mock private OrderRepository orderRepository;
    @Mock private StripeService stripeService;
    @Mock private ProcessedStripeEventRepository eventRepository;

    @InjectMocks private PaymentService paymentService;

    /**
     * createPendingOrder has already decremented stock by the time we call Stripe.
     * If the network call then fails there is no order anyone can pay for, so the
     * reservation has to be handed back — otherwise every Stripe blip permanently
     * burns inventory with nothing to show for it.
     */
    @Test
    void initiateCheckout_releasesReservedStockWhenStripeIntentCreationFails() throws StripeException {
        Order order = Order.builder().id(9L).status(OrderStatus.PENDING_PAYMENT).build();
        when(orderService.findPendingOrder("alice")).thenReturn(Optional.empty());
        when(orderService.createPendingOrder("alice")).thenReturn(order);
        when(stripeService.createPaymentIntent(order))
                .thenThrow(new ApiConnectionException("stripe unreachable"));

        assertThrows(StripeException.class, () -> paymentService.initiateCheckout("alice"));

        verify(orderService).cancelAndReleaseStock(9L);
        // And nothing half-finished is left behind pointing at an intent that never existed.
        verify(orderService, never()).attachPaymentIntent(anyLong(), anyString());
    }
}
