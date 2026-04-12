package com.ajcarpinello.Pern_Merch_Website.service;

import com.ajcarpinello.Pern_Merch_Website.dto.CartItemDTO;
import com.ajcarpinello.Pern_Merch_Website.entity.CartItem;
import com.ajcarpinello.Pern_Merch_Website.entity.Product;
import com.ajcarpinello.Pern_Merch_Website.entity.User;
import com.ajcarpinello.Pern_Merch_Website.repository.CartItemRepository;
import com.ajcarpinello.Pern_Merch_Website.repository.ProductRepository;
import com.ajcarpinello.Pern_Merch_Website.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public List<CartItemDTO> getCart(String username) {
        User user = findUser(username);
        List<CartItem> cartItems = cartItemRepository.findByUser(user);
        List<CartItemDTO> cartItemDTOS = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            cartItemDTOS.add(toDTO(cartItem));
        }
        return cartItemDTOS;
    }

    public CartItemDTO addToCart(String username, Long productId, int quantity) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Product Id not found"));
        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Not enough items in stock for: " + product.getName());
        }
        User user = findUser(username);

        Optional<CartItem> cartItemOptional = cartItemRepository.findByUserAndProductId(user, productId);
        CartItem cartItem;
        if (cartItemOptional.isPresent()) {
            cartItem = cartItemOptional.get();
            if (product.getStockQuantity() < cartItem.getQuantity() + quantity) {
                throw new RuntimeException("Not enough items in stock for: " + product.getName());
            }
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        } else {
            cartItem = CartItem.builder()
                    .user(user)
                    .product(product)
                    .quantity(quantity).build();
        }
        cartItemRepository.save(cartItem);
        return toDTO(cartItem);
    }

    public CartItemDTO updateQuantity(String username, Long cartItemId, Integer quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!cartItem.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized");
        }

        if (cartItem.getProduct().getStockQuantity() < quantity) {
            throw new RuntimeException("Not enough items in stock for: " + cartItem.getProduct().getName());
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
                .orElseThrow(() -> new RuntimeException("cartItemId not found"));

        if (!cartItem.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized");
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
                .productId(cartItem.getProduct().getId())
                .productName(cartItem.getProduct().getName())
                .productImageUrl(cartItem.getProduct().getImageUrl())
                .productPrice(cartItem.getProduct().getPrice())
                .size(cartItem.getProduct().getSize())
                .quantity(cartItem.getQuantity())
                .stockQuantity(cartItem.getProduct().getStockQuantity())
                .build();
    }
}