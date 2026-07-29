package com.example.fooddelivery.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {

    @Schema(example = "1")
    private Long menuId;

    @Schema(example = "Margherita Pizza")
    private String menuName;

    @Schema(example = "299.0")
    private Double price;

    @Schema(example = "2")
    private Integer quantity;

    @Schema(example = "598.0")
    private Double subtotal;

}
