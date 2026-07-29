package com.example.fooddelivery.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantRequest {

    @Schema(example = "Pizza Palace")
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Schema(example = "Best pizza in town with authentic Italian recipes")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Schema(example = "123 Main Street")
    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    @Schema(example = "Mumbai")
    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Schema(example = "Italian")
    @NotBlank(message = "Cuisine is required")
    @Size(max = 100, message = "Cuisine must not exceed 100 characters")
    private String cuisine;

    @Schema(example = "https://example.com/pizza-palace.jpg")
    @URL(message = "Image URL must be a valid URL")
    private String imageUrl;

}
