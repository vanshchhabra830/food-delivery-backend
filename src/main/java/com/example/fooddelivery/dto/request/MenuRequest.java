package com.example.fooddelivery.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class MenuRequest {

    @Schema(example = "Margherita Pizza")
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Schema(example = "Classic delight with 100% real mozzarella cheese")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Schema(example = "12.99")
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than zero")
    private Double price;

    @Schema(example = "Pizza")
    @NotBlank(message = "Category is required")
    @Size(max = 50, message = "Category must not exceed 50 characters")
    private String category;

    @Schema(example = "https://example.com/margherita.jpg")
    @URL(message = "Image URL must be a valid URL")
    private String imageUrl;

}
