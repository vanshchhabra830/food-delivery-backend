package com.example.fooddelivery.repository;

import com.example.fooddelivery.entity.Payment;
import com.example.fooddelivery.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    boolean existsByOrderIdAndPaymentStatus(Long orderId, PaymentStatus paymentStatus);

    List<Payment> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Payment> findByIdAndUserId(Long id, Long userId);

    Optional<Payment> findFirstByOrderIdAndUserIdOrderByCreatedAtDesc(Long orderId, Long userId);

}
