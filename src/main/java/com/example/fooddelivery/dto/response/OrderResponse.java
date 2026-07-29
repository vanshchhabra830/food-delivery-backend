package com.example.fooddelivery.dto.response;

import com.example.fooddelivery.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    @Schema(example = "1")
    private Long id;

    private String deliveryAddress;

    private List<OrderItemResponse> items;

    @Schema(example = "598.0")
    private Double totalAmount;

    private OrderStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
