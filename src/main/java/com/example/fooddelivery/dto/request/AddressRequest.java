package com.example.fooddelivery.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {

    @Schema(example = "John Doe")
    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must be at most 100 characters")
    private String fullName;

    @Schema(example = "1234567890")
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\d{10}$", message = "Phone number must be exactly 10 digits")
    private String phoneNumber;

    @Schema(example = "123 Main St")
    @NotBlank(message = "Address Line 1 is required")
    @Size(max = 255, message = "Address Line 1 must be at most 255 characters")
    private String addressLine1;

    @Schema(example = "Apt 4B")
    private String addressLine2;

    @Schema(example = "Near Central Park")
    private String landmark;

    @Schema(example = "New York")
    @NotBlank(message = "City is required")
    private String city;

    @Schema(example = "NY")
    @NotBlank(message = "State is required")
    private String state;

    @Schema(example = "10001")
    @NotBlank(message = "Postal code is required")
    private String postalCode;

    @Schema(example = "USA")
    @NotBlank(message = "Country is required")
    private String country;

    @Schema(example = "true")
    @NotNull(message = "isDefault flag is required")
    private Boolean isDefault;

}
