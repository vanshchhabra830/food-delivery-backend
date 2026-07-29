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
public class RestaurantResponse {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "Pizza Palace")
    private String name;

    @Schema(example = "Best pizza in town with authentic Italian recipes")
    private String description;

    @Schema(example = "123 Main Street")
    private String address;

    @Schema(example = "Mumbai")
    private String city;

    @Schema(example = "Italian")
    private String cuisine;

    @Schema(example = "https://example.com/pizza-palace.jpg")
    private String imageUrl;

    @Schema(example = "4.5")
    private Double rating;

    @Schema(example = "true")
    private Boolean active;

}
