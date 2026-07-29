package com.example.fooddelivery.service.impl;

import com.example.fooddelivery.dto.request.PaymentRequest;
import com.example.fooddelivery.dto.response.PaymentResponse;
import com.example.fooddelivery.entity.Order;
import com.example.fooddelivery.entity.Payment;
import com.example.fooddelivery.entity.User;
import com.example.fooddelivery.enums.OrderStatus;
import com.example.fooddelivery.enums.PaymentStatus;
import com.example.fooddelivery.exception.DuplicateResourceException;
import com.example.fooddelivery.exception.ResourceNotFoundException;
import com.example.fooddelivery.mapper.PaymentMapper;
import com.example.fooddelivery.repository.OrderRepository;
import com.example.fooddelivery.repository.PaymentRepository;
import com.example.fooddelivery.repository.UserRepository;
import com.example.fooddelivery.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              OrderRepository orderRepository,
                              UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public PaymentResponse processPayment(Long orderId, PaymentRequest request) {
        User user = getCurrentUser();
        Order order = findUserOrder(orderId, user.getId());

        if (paymentRepository.existsByOrderIdAndPaymentStatus(orderId, PaymentStatus.SUCCESS)) {
            log.warn("Payment rejected: order {} is already paid", orderId);
            throw new DuplicateResourceException("Order is already paid");
        }

        PaymentStatus paymentStatus = Boolean.TRUE.equals(request.getSimulateSuccess())
                ? PaymentStatus.SUCCESS
                : PaymentStatus.FAILED;

        Payment payment = Payment.builder()
                .order(order)
                .user(user)
                .amount(order.getTotalAmount())
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(paymentStatus)
                .transactionId(generateTransactionId())
                .paymentTime(LocalDateTime.now())
                .build();

        if (paymentStatus == PaymentStatus.SUCCESS) {
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
            log.info("Payment successful for order id: {}, transactionId: {}", orderId, payment.getTransactionId());
        } else {
            log.info("Payment failed for order id: {}, transactionId: {}", orderId, payment.getTransactionId());
        }

        Payment savedPayment = paymentRepository.save(payment);
        return PaymentMapper.toPaymentResponse(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long paymentId) {
        User user = getCurrentUser();
        Payment payment = paymentRepository.findByIdAndUserId(paymentId, user.getId())
                .orElseThrow(() -> {
                    log.warn("Unauthorized access or payment not found. PaymentId: {}, UserId: {}",
                            paymentId, user.getId());
                    return new ResourceNotFoundException("Payment not found or does not belong to user");
                });
        return PaymentMapper.toPaymentResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(Long orderId) {
        User user = getCurrentUser();
        findUserOrder(orderId, user.getId());

        Payment payment = paymentRepository.findFirstByOrderIdAndUserIdOrderByCreatedAtDesc(orderId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order id: " + orderId));

        return PaymentMapper.toPaymentResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentHistory() {
        User user = getCurrentUser();
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(PaymentMapper::toPaymentResponse)
                .toList();
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Order findUserOrder(Long orderId, Long userId) {
        return orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> {
                    log.warn("Unauthorized access attempt or order not found. OrderId: {}, UserId: {}", orderId, userId);
                    return new ResourceNotFoundException("Order not found or does not belong to user");
                });
    }

    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().replace("-", "");
    }

}
