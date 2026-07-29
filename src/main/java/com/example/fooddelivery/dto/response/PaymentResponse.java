package com.example.fooddelivery.dto.response;

import com.example.fooddelivery.enums.PaymentMethod;
import com.example.fooddelivery.enums.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "1")
    private Long orderId;

    @Schema(example = "598.0")
    private Double amount;

    private PaymentMethod paymentMethod;

    private PaymentStatus paymentStatus;

    @Schema(example = "TXN-a1b2c3d4e5f6")
    private String transactionId;

    private LocalDateTime paymentTime;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
