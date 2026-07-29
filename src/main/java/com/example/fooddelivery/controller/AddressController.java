package com.example.fooddelivery.controller;

import com.example.fooddelivery.dto.request.AddressRequest;
import com.example.fooddelivery.dto.response.AddressResponse;
import com.example.fooddelivery.exception.ErrorResponse;
import com.example.fooddelivery.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/addresses")
@Tag(name = "Address", description = "Address management endpoints")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @Operation(summary = "Add a new address")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Address created successfully",
                    content = @Content(schema = @Schema(implementation = AddressResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping
    public ResponseEntity<AddressResponse> addAddress(@Valid @RequestBody AddressRequest request) {
        AddressResponse response = addressService.addAddress(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get all addresses for the authenticated user")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Addresses retrieved successfully"
            )
    })
    @GetMapping
    public ResponseEntity<List<AddressResponse>> getAllAddresses() {
        return ResponseEntity.ok(addressService.getAllAddresses());
    }

    @Operation(summary = "Get a specific address by ID")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Address retrieved successfully",
                    content = @Content(schema = @Schema(implementation = AddressResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Address not found or unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/{addressId}")
    public ResponseEntity<AddressResponse> getAddressById(
            @Parameter(description = "Address ID") @PathVariable Long addressId) {
        return ResponseEntity.ok(addressService.getAddressById(addressId));
    }

    @Operation(summary = "Update an existing address")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Address updated successfully",
                    content = @Content(schema = @Schema(implementation = AddressResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed or manual default unset attempt",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Address not found or unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PutMapping("/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(
            @Parameter(description = "Address ID") @PathVariable Long addressId,
            @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.ok(addressService.updateAddress(addressId, request));
    }

    @Operation(summary = "Delete an address")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Address deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Address not found or unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            @Parameter(description = "Address ID") @PathVariable Long addressId) {
        addressService.deleteAddress(addressId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Set an address as default")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Address set as default successfully",
                    content = @Content(schema = @Schema(implementation = AddressResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Address not found or unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PatchMapping("/{addressId}/default")
    public ResponseEntity<AddressResponse> setDefaultAddress(
            @Parameter(description = "Address ID") @PathVariable Long addressId) {
        return ResponseEntity.ok(addressService.setDefaultAddress(addressId));
    }

}
