package com.ajcarpinello.Pern_Merch_Website.service;

import com.ajcarpinello.Pern_Merch_Website.dto.OrderDTO;
import com.ajcarpinello.Pern_Merch_Website.dto.OrderItemDTO;
import com.ajcarpinello.Pern_Merch_Website.entity.*;
import com.ajcarpinello.Pern_Merch_Website.exception.AppException;
import com.ajcarpinello.Pern_Merch_Website.repository.CartItemRepository;
import com.ajcarpinello.Pern_Merch_Website.repository.OrderRepository;
import com.ajcarpinello.Pern_Merch_Website.repository.ProductVariantRepository;
import com.ajcarpinello.Pern_Merch_Website.repository.UserRepository;
import com.stripe.model.PaymentIntent;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final CartService cartService;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final ProductVariantRepository variantRepository;
    private final UserService userService;

    // @Transactional ensures that saving the order and clearing the cart happen as an "all-or-nothing" operation.
    // If one step fails (e.g. database error), everything rolls back, preventing inconsistent states.
    @Transactional
    public Order createPendingOrder(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "User not found"));
        
        List<CartItem> cartItems = cartItemRepository.findByUser(user);
        if (cartItems.isEmpty()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Cart is empty");
        }

        Order order = new Order();
        BigDecimal priceSum = BigDecimal.ZERO;
        for (CartItem cartItem : cartItems) {
            /* Re-fetch the product with a pessimistic lock so no other transaction
               can modify its stock until this checkout transaction completes. */
            // prevents a race condition where both users purchase an item when there's only one left
            ProductVariant variant = variantRepository.findByIdForUpdate(cartItem.getVariant().getId())
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Variant no longer exists"));
            Product product = variant.getProduct();

            if (cartItem.getQuantity() > variant.getStockQuantity()) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Not enough stock for: " + variant.getSize() + " " + product.getName());
            }
            variant.setStockQuantity(variant.getStockQuantity() - cartItem.getQuantity());

            /* If we fetch an existing entity inside a @Transactional method and
               modify any of its fields, JPA automatically issues an UPDATE when the transaction commits (method ends). */
            // so we dont need to save product

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .variant(variant)
                    .quantity(cartItem.getQuantity())
                    .priceAtPurchase(product.getPrice()).build();
            order.getItems().add(orderItem);
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            priceSum = priceSum.add(itemTotal);
        }
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(priceSum);
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        return orderRepository.save(order);
    }

    @Transactional
    public void finalizeOrder(PaymentIntent intent) {
        Order order = orderRepository.findByStripePaymentIntentId(intent.getId())
                .orElseThrow(() -> new IllegalStateException("Order not found for PI: " + intent.getId()));

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) return; // order-level idempotency

        // Shipping was attached to the PaymentIntent by the frontend's confirmPayment call
        // (via the Stripe AddressElement). Snapshot it onto the order, and sync the user's
        // default if it differs from what they just used.
        Address shipping = toAddress(intent.getShipping());
        if (shipping != null) {
            order.setShippingAddress(shipping);
            User user = order.getUser();
            if (!Objects.equals(user.getDefaultShippingAddress(), shipping)) {
                user.setDefaultShippingAddress(shipping);
            }
        } else {
            log.warn("payment_intent.succeeded for PI {} arrived without shipping — order {} marked PAID with no address",
                intent.getId(), order.getId());
        }

        order.setStatus(OrderStatus.PAID);
        order.setPaidAt(LocalDateTime.now());
        orderRepository.save(order);
        cartService.clearCart(order.getUser().getUsername());
    }

    private Address toAddress(com.stripe.model.ShippingDetails shipping) {
        if (shipping == null || shipping.getAddress() == null) return null;
        com.stripe.model.Address a = shipping.getAddress();
        return Address.builder()
                .recipientName(shipping.getName())
                .line1(a.getLine1())
                .line2(a.getLine2())
                .city(a.getCity())
                .state(a.getState())
                .postalCode(a.getPostalCode())
                .country(a.getCountry())
                .phone(shipping.getPhone())
                .build();
    }

    /**
     * Saves the Stripe PaymentIntent ID onto the order. Separate short transaction
     * so it can run after the (network) Stripe API call.
     */
    @Transactional
    public void attachPaymentIntent(Long orderId, String paymentIntentId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("Order not found: " + orderId));
        order.setStripePaymentIntentId(paymentIntentId);
        orderRepository.save(order);
    }

    /**
     * Compensating transaction: release stock, mark order CANCELLED.
     * Called when Stripe intent creation fails.
     */
    @Transactional
    public void cancelAndReleaseStock(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("Order not found: " + orderId));
        cancelAndReleaseStockInternal(order);
    }

    private void cancelAndReleaseStockInternal(Order order) {
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) return; // already handled
        for (OrderItem item : order.getItems()) {
            ProductVariant variant = variantRepository.findByIdForUpdate(item.getVariant().getId())
                    .orElseThrow();
            variant.setStockQuantity(variant.getStockQuantity() + item.getQuantity());
        }
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public boolean pendingOrderCartMatches(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("Order not found: " + orderId));
        return cartMatchesOrder(order.getUser(), order);
    }

    /** Cart contents (productId + quantity) match the order's items exactly. */
    private boolean cartMatchesOrder(User user, Order order) {
        List<CartItem> cartItems = cartItemRepository.findByUser(user);
        if (cartItems.size() != order.getItems().size()) return false;

        Map<Long, Integer> cartMap = new HashMap<>();
        for (CartItem ci : cartItems) {
            cartMap.put(ci.getVariant().getId(), ci.getQuantity());
        }

        for (OrderItem oi : order.getItems()) {
            Integer cartQty = cartMap.get(oi.getVariant().getId());
            if (cartQty == null || cartQty != oi.getQuantity()) return false;
        }
        return true;
    }

    /** The caller's current awaiting-payment order, if any. */
    public Optional<Order> findPendingOrder(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "User not found"));
        return orderRepository.findFirstByUserAndStatusOrderByOrderDateDesc(user, OrderStatus.PENDING_PAYMENT);
    }

    public List<OrderDTO> getOrderHistory(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "User not found"));
        List<Order> orders = orderRepository.findByUserOrderByOrderDateDesc(user);
        List<OrderDTO> orderDTOS = new ArrayList<>();
        for (Order order : orders) {
            if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
                orderDTOS.add(toOrderDTO(order));
            }
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
        if (order.getShippingAddress() != null) {
            orderDTO.setShippingAddress(userService.toAddressDTO(order.getShippingAddress()));
        }
        return orderDTO;
    }

    private OrderItemDTO toOrderItemDTO(OrderItem orderItem) {
        return OrderItemDTO.builder()
                .productId(orderItem.getVariant().getProduct().getId())
                .productName(orderItem.getVariant().getProduct().getName())
                .size(orderItem.getVariant().getSize())
                .quantity(orderItem.getQuantity())
                .priceAtPurchase(orderItem.getPriceAtPurchase()).build();
    }
}
