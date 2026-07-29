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
public class CartItemResponse {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "5")
    private Long menuId;

    @Schema(example = "Margherita Pizza")
    private String menuName;

    @Schema(example = "12.99")
    private Double menuPrice;

    @Schema(example = "2")
    private Integer quantity;

    @Schema(example = "25.98")
    private Double subtotal;

}
