package com.ajcarpinello.Pern_Merch_Website.service;

import com.ajcarpinello.Pern_Merch_Website.dto.OrderDTO;
import com.ajcarpinello.Pern_Merch_Website.dto.OrderItemDTO;
import com.ajcarpinello.Pern_Merch_Website.entity.*;
import com.ajcarpinello.Pern_Merch_Website.repository.CartItemRepository;
import com.ajcarpinello.Pern_Merch_Website.repository.OrderRepository;
import com.ajcarpinello.Pern_Merch_Website.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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

    // @Transactional ensures that saving the order and clearing the cart happen as an "all-or-nothing" operation.
    // If one step fails (e.g. database error), everything rolls back, preventing inconsistent states.
    @Transactional
    public OrderDTO checkout(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Username does not exist"));
        List<CartItem> cartItems = cartItemRepository.findByUser(user);
        Order order = new Order();
        BigDecimal priceSum = BigDecimal.ZERO;
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(cartItem.getProduct())
                    .quantity(cartItem.getQuantity())
                    .priceAtPurchase(cartItem.getProduct().getPrice()).build();
            order.getItems().add(orderItem);
            BigDecimal itemTotal = cartItem.getProduct().getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
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
                .orElseThrow(() -> new RuntimeException("Username not found"));
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
