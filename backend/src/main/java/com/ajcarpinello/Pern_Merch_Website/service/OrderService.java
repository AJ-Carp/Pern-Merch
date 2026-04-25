package com.ajcarpinello.Pern_Merch_Website.service;

import com.ajcarpinello.Pern_Merch_Website.dto.OrderDTO;
import com.ajcarpinello.Pern_Merch_Website.dto.OrderItemDTO;
import com.ajcarpinello.Pern_Merch_Website.entity.*;
import com.ajcarpinello.Pern_Merch_Website.exception.AppException;
import com.ajcarpinello.Pern_Merch_Website.repository.CartItemRepository;
import com.ajcarpinello.Pern_Merch_Website.repository.OrderRepository;
import com.ajcarpinello.Pern_Merch_Website.repository.ProductRepository;
import com.ajcarpinello.Pern_Merch_Website.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CartService cartService;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    // @Transactional ensures that saving the order and clearing the cart happen as an "all-or-nothing" operation.
    // If one step fails (e.g. database error), everything rolls back, preventing inconsistent states.
    @Transactional
    public OrderDTO checkout(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "User not found"));
        List<CartItem> cartItems = cartItemRepository.findByUser(user);
        Order order = new Order();
        BigDecimal priceSum = BigDecimal.ZERO;
        for (CartItem cartItem : cartItems) {
            /* Re-fetch the product with a pessimistic lock so no other transaction
               can modify its stock until this checkout transaction completes. */
            // prevents a race condition where both users purchase an item when there's only one left
            Product product = productRepository.findByIdForUpdate(cartItem.getProduct().getId())
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Product no longer exists"));

            if (cartItem.getQuantity() > product.getStockQuantity()) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Not enough stock for: " + product.getName());
            }
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());

            /* If we fetch an existing entity inside a @Transactional method and
               modify any of its fields, JPA automatically issues an UPDATE when the transaction commits (method ends). */
            // so we dont need to save product

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .priceAtPurchase(product.getPrice()).build();
            order.getItems().add(orderItem);
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            priceSum = priceSum.add(itemTotal);
        }
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(priceSum);
        order.setStatus(OrderStatus.PENDING);
        orderRepository.save(order);
        cartService.clearCart(username);
        return toOrderDTO(order);
    }

    public List<OrderDTO> getOrderHistory(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "User not found"));
        List<Order> orders = orderRepository.findByUserOrderByOrderDateDesc(user);
        List<OrderDTO> orderDTOS = new ArrayList<>();
        for (Order order : orders) {
            orderDTOS.add(toOrderDTO(order));
        }
        return orderDTOS;
    }

    private OrderDTO toOrderDTO(Order order) {
        OrderDTO orderDTO = OrderDTO.builder()
                .id(order.getId())
                .orderDate(order.getOrderDate())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .build();
        List<OrderItem> orderItems = order.getItems();
        List<OrderItemDTO> orderItemDTOs = new ArrayList<>();
        for (OrderItem orderItem : orderItems)  {
            orderItemDTOs.add(toOrderItemDTO(orderItem));
        }
        orderDTO.setItems(orderItemDTOs);
        return orderDTO;
    }

    private OrderItemDTO toOrderItemDTO(OrderItem orderItem) {
        return OrderItemDTO.builder()
                .productId(orderItem.getProduct().getId())
                .productName(orderItem.getProduct().getName())
                .quantity(orderItem.getQuantity())
                .priceAtPurchase(orderItem.getPriceAtPurchase()).build();
    }

}
