package com.example.fooddelivery.service;

import com.example.fooddelivery.dto.request.OrderRequest;
import com.example.fooddelivery.dto.response.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(OrderRequest request);

    List<OrderResponse> getMyOrders();

    OrderResponse getOrderById(Long orderId);

    OrderResponse cancelOrder(Long orderId);

}
