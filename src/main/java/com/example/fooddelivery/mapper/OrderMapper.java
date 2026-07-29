package com.example.fooddelivery.mapper;

import com.example.fooddelivery.dto.response.OrderItemResponse;
import com.example.fooddelivery.dto.response.OrderResponse;
import com.example.fooddelivery.entity.Order;
import com.example.fooddelivery.entity.OrderItem;

import java.util.Collections;
import java.util.List;

public class OrderMapper {

    private OrderMapper() {
    }

    public static OrderResponse toOrderResponse(Order order) {
        if (order == null) {
            return null;
        }

        List<OrderItemResponse> items = order.getOrderItems() != null
                ? order.getOrderItems().stream()
                        .map(OrderMapper::toOrderItemResponse)
                        .toList()
                : Collections.emptyList();

        return OrderResponse.builder()
                .id(order.getId())
                .deliveryAddress(order.getDeliveryAddress())
                .items(items)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    public static OrderItemResponse toOrderItemResponse(OrderItem orderItem) {
        if (orderItem == null) {
            return null;
        }

        return OrderItemResponse.builder()
                .menuId(orderItem.getMenuId())
                .menuName(orderItem.getMenuName())
                .price(orderItem.getPrice())
                .quantity(orderItem.getQuantity())
                .subtotal(orderItem.getSubtotal())
                .build();
    }

}
