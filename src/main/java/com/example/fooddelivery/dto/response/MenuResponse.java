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
public class MenuResponse {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "Margherita Pizza")
    private String name;

    @Schema(example = "Classic delight with 100% real mozzarella cheese")
    private String description;

    @Schema(example = "12.99")
    private Double price;

    @Schema(example = "Pizza")
    private String category;

    @Schema(example = "https://example.com/margherita.jpg")
    private String imageUrl;

    @Schema(example = "true")
    private Boolean available;

    @Schema(example = "1")
    private Long restaurantId;

}
