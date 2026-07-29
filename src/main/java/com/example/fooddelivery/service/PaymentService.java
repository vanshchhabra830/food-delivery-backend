package com.example.fooddelivery.service;

import com.example.fooddelivery.dto.request.PaymentRequest;
import com.example.fooddelivery.dto.response.PaymentResponse;

import java.util.List;

public interface PaymentService {

    PaymentResponse processPayment(Long orderId, PaymentRequest request);

    PaymentResponse getPaymentById(Long paymentId);

    PaymentResponse getPaymentByOrderId(Long orderId);

    List<PaymentResponse> getPaymentHistory();

}
