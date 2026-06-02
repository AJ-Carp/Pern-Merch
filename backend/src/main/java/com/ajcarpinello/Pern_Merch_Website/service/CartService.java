package com.ajcarpinello.Pern_Merch_Website.service;

import com.ajcarpinello.Pern_Merch_Website.dto.CartItemDTO;
import com.ajcarpinello.Pern_Merch_Website.entity.CartItem;
import com.ajcarpinello.Pern_Merch_Website.entity.ProductVariant;
import com.ajcarpinello.Pern_Merch_Website.entity.User;
import com.ajcarpinello.Pern_Merch_Website.exception.AppException;
import com.ajcarpinello.Pern_Merch_Website.repository.CartItemRepository;
import com.ajcarpinello.Pern_Merch_Website.repository.ProductVariantRepository;
import com.ajcarpinello.Pern_Merch_Website.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository variantRepository;

    public List<CartItemDTO> getCart(String username) {
        User user = findUser(username);
        List<CartItem> cartItems = cartItemRepository.findByUser(user);
        List<CartItemDTO> cartItemDTOS = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            cartItemDTOS.add(toDTO(cartItem));
        }
        return cartItemDTOS;
    }

    public CartItemDTO addToCart(String username, Long variantId, int quantity) {
        ProductVariant variant = variantRepository.findById(variantId).orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "variant not found"));
        if (variant.getStockQuantity() < quantity) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Not enough items in stock for: " + variant.getSize() + " " + variant.getProduct().getName());
        }
        User user = findUser(username);

        Optional<CartItem> cartItemOptional = cartItemRepository.findByUserAndVariantId(user, variantId);
        CartItem cartItem;
        if (cartItemOptional.isPresent()) {
            cartItem = cartItemOptional.get();
            if (variant.getStockQuantity() < cartItem.getQuantity() + quantity) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Not enough items in stock for: " + variant.getSize() + " " + variant.getProduct().getName());
            }
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        } else {
            cartItem = CartItem.builder()
                    .user(user)
                    .variant(variant)
                    .quantity(quantity).build();
        }
        cartItemRepository.save(cartItem);
        return toDTO(cartItem);
    }

    public CartItemDTO updateQuantity(String username, Long cartItemId, Integer quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Cart item not found"));

        if (!cartItem.getUser().getUsername().equals(username)) {
            throw new AppException(HttpStatus.FORBIDDEN, "Access denied");
        }

        if (cartItem.getVariant().getStockQuantity() < quantity) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Not enough items in stock for: " + 
                cartItem.getVariant().getSize() + " " + cartItem.getVariant().getProduct().getName());
        }

        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
            return null;
        }

        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);
        return toDTO(cartItem);
    }

    public void removeFromCart(String username, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Cart item not found"));

        if (!cartItem.getUser().getUsername().equals(username)) {
            throw new AppException(HttpStatus.FORBIDDEN, "Access denied");
        }
        cartItemRepository.delete(cartItem);
    }

    /* If deleteByUser starts deleting cart items and something fails partway through (like a database error),
    the @Transactional annotation ensures that all deletions are rolled back—so either all cart items are deleted,
    or none are. No partial deletes will remain. */
    // @Transactional required because deleteByUser executes a SELECT + multiple DELETEs internally
    @Transactional
    public void clearCart(String username) {
        User user = findUser(username);
        cartItemRepository.deleteByUser(user);
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Username not found"));
    }

    private CartItemDTO toDTO(CartItem cartItem) {
        return CartItemDTO.builder()
                .id(cartItem.getId())
                .productId(cartItem.getVariant().getProduct().getId())
                .productName(cartItem.getVariant().getProduct().getName())
                .productImageUrl(cartItem.getVariant().getProduct().getImageUrl())
                .productPrice(cartItem.getVariant().getProduct().getPrice())
                .size(cartItem.getVariant().getSize())
                .quantity(cartItem.getQuantity())
                .stockQuantity(cartItem.getVariant().getStockQuantity())
                .build();
    }
}