package com.example.fooddelivery.service.impl;

import com.example.fooddelivery.dto.request.OrderRequest;
import com.example.fooddelivery.dto.response.OrderResponse;
import com.example.fooddelivery.entity.Address;
import com.example.fooddelivery.entity.Cart;
import com.example.fooddelivery.entity.CartItem;
import com.example.fooddelivery.entity.Menu;
import com.example.fooddelivery.entity.Order;
import com.example.fooddelivery.entity.OrderItem;
import com.example.fooddelivery.entity.User;
import com.example.fooddelivery.enums.OrderStatus;
import com.example.fooddelivery.exception.ResourceNotFoundException;
import com.example.fooddelivery.mapper.OrderMapper;
import com.example.fooddelivery.repository.AddressRepository;
import com.example.fooddelivery.repository.CartRepository;
import com.example.fooddelivery.repository.MenuRepository;
import com.example.fooddelivery.repository.OrderRepository;
import com.example.fooddelivery.repository.UserRepository;
import com.example.fooddelivery.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final MenuRepository menuRepository;
    private final UserRepository userRepository;

    public OrderServiceImpl(OrderRepository orderRepository,
                            CartRepository cartRepository,
                            AddressRepository addressRepository,
                            MenuRepository menuRepository,
                            UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.addressRepository = addressRepository;
        this.menuRepository = menuRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        User user = getCurrentUser();

        Address address = addressRepository.findByIdAndUserId(request.getAddressId(), user.getId())
                .orElseThrow(() -> {
                    log.warn("Unauthorized access or invalid address. AddressId: {}, UserId: {}",
                            request.getAddressId(), user.getId());
                    return new ResourceNotFoundException("Address not found or does not belong to user");
                });

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> {
                    log.warn("Cart not found for user: {}", user.getEmail());
                    return new ResourceNotFoundException("Cart not found");
                });

        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            log.warn("Order creation failed: cart is empty for user: {}", user.getEmail());
            throw new IllegalArgumentException("Cart is empty");
        }

        String deliveryAddressSnapshot = buildDeliveryAddress(address);
        List<OrderItem> orderItems = new ArrayList<>();
        double totalAmount = 0.0;

        for (CartItem cartItem : cart.getCartItems()) {
            Menu menu = menuRepository.findById(cartItem.getMenu().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Menu not found with id: " + cartItem.getMenu().getId()));

            if (!menu.getAvailable()) {
                log.warn("Order creation failed: menu item unavailable: {}", menu.getName());
                throw new IllegalArgumentException("Menu item is not available: " + menu.getName());
            }

            if (!menu.getRestaurant().getActive()) {
                log.warn("Order creation failed: restaurant inactive: {}", menu.getRestaurant().getName());
                throw new IllegalArgumentException("Restaurant is not active: " + menu.getRestaurant().getName());
            }

            OrderItem orderItem = OrderItem.builder()
                    .menuId(menu.getId())
                    .menuName(menu.getName())
                    .price(menu.getPrice())
                    .quantity(cartItem.getQuantity())
                    .subtotal(cartItem.getSubtotal())
                    .build();
            orderItems.add(orderItem);
            totalAmount += cartItem.getSubtotal();
        }

        Order order = Order.builder()
                .user(user)
                .deliveryAddress(deliveryAddressSnapshot)
                .totalAmount(totalAmount)
                .status(OrderStatus.PLACED)
                .build();

        for (OrderItem orderItem : orderItems) {
            orderItem.setOrder(order);
            order.getOrderItems().add(orderItem);
        }

        Order savedOrder = orderRepository.save(order);

        cart.getCartItems().clear();
        cart.setTotalAmount(0.0);
        cartRepository.save(cart);

        log.info("Order created with id: {} for user: {}", savedOrder.getId(), user.getEmail());
        return OrderMapper.toOrderResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders() {
        User user = getCurrentUser();
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(OrderMapper::toOrderResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        User user = getCurrentUser();
        Order order = findUserOrder(orderId, user.getId());
        return OrderMapper.toOrderResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        User user = getCurrentUser();
        Order order = findUserOrder(orderId, user.getId());

        if (order.getStatus() != OrderStatus.PLACED && order.getStatus() != OrderStatus.CONFIRMED) {
            log.warn("Invalid order cancellation attempt. OrderId: {}, Status: {}", orderId, order.getStatus());
            throw new IllegalArgumentException(
                    "Order cannot be cancelled in status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.save(order);
        log.info("Order cancelled with id: {} for user: {}", orderId, user.getEmail());
        return OrderMapper.toOrderResponse(savedOrder);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private String buildDeliveryAddress(Address address) {
        StringBuilder sb = new StringBuilder();
        sb.append(address.getFullName()).append(", ").append(address.getPhoneNumber()).append("\n");
        sb.append(address.getAddressLine1());
        if (address.getAddressLine2() != null && !address.getAddressLine2().isBlank()) {
            sb.append(", ").append(address.getAddressLine2());
        }
        if (address.getLandmark() != null && !address.getLandmark().isBlank()) {
            sb.append(" (").append(address.getLandmark()).append(")");
        }
        sb.append("\n")
                .append(address.getCity()).append(", ")
                .append(address.getState()).append(" ")
                .append(address.getPostalCode()).append("\n")
                .append(address.getCountry());
        return sb.toString();
    }

    private Order findUserOrder(Long orderId, Long userId) {
        return orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> {
                    log.warn("Unauthorized access attempt or order not found. OrderId: {}, UserId: {}", orderId, userId);
                    return new ResourceNotFoundException("Order not found or does not belong to user");
                });
    }

}
