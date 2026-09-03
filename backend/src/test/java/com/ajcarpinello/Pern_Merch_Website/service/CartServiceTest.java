package com.ajcarpinello.Pern_Merch_Website.service;

import com.ajcarpinello.Pern_Merch_Website.entity.CartItem;
import com.ajcarpinello.Pern_Merch_Website.entity.User;
import com.ajcarpinello.Pern_Merch_Website.exception.AppException;
import com.ajcarpinello.Pern_Merch_Website.repository.CartItemRepository;
import com.ajcarpinello.Pern_Merch_Website.repository.ProductVariantRepository;
import com.ajcarpinello.Pern_Merch_Website.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductVariantRepository variantRepository;

    @InjectMocks private CartService cartService;

    /**
     * Cart item IDs are sequential and come straight off the URL, so the ownership
     * check in the service is the only thing standing between a logged-in user and
     * someone else's cart. Route-level auth can't catch this — the caller is
     * authenticated, just not entitled to this row.
     */
    @Test
    void removeFromCart_refusesToDeleteAnotherUsersCartItem() {
        User owner = User.builder().id(1L).username("alice").build();
        CartItem item = CartItem.builder().id(5L).user(owner).quantity(1).build();
        when(cartItemRepository.findById(5L)).thenReturn(Optional.of(item));

        AppException ex = assertThrows(AppException.class,
                () -> cartService.removeFromCart("mallory", 5L));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        verify(cartItemRepository, never()).delete(any());
    }
}
