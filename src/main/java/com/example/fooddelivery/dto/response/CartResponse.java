package com.example.fooddelivery.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponse {

    @Schema(example = "1")
    private Long id;

    private List<CartItemResponse> items;

    @Schema(example = "5")
    private Integer totalItems;

    @Schema(example = "249.95")
    private Double totalAmount;

}
