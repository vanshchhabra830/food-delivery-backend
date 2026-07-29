package com.example.fooddelivery.dto.request;

import com.example.fooddelivery.enums.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    @Schema(example = "UPI")
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @Schema(example = "true")
    @NotNull(message = "simulateSuccess flag is required")
    private Boolean simulateSuccess;

}
