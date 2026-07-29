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
public class AddressResponse {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "John Doe")
    private String fullName;

    @Schema(example = "1234567890")
    private String phoneNumber;

    @Schema(example = "123 Main St")
    private String addressLine1;

    @Schema(example = "Apt 4B")
    private String addressLine2;

    @Schema(example = "Near Central Park")
    private String landmark;

    @Schema(example = "New York")
    private String city;

    @Schema(example = "NY")
    private String state;

    @Schema(example = "10001")
    private String postalCode;

    @Schema(example = "USA")
    private String country;

    @Schema(example = "true")
    private Boolean isDefault;

}
