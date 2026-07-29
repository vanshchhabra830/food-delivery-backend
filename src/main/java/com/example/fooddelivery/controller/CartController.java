package com.example.fooddelivery.controller;

import com.example.fooddelivery.dto.request.CartItemRequest;
import com.example.fooddelivery.dto.request.CartItemUpdateRequest;
import com.example.fooddelivery.dto.response.CartResponse;
import com.example.fooddelivery.exception.ErrorResponse;
import com.example.fooddelivery.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cart")
@Tag(name = "Cart", description = "Shopping cart management endpoints")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @Operation(
            summary = "Add item to cart",
            description = "Adds a menu item to the authenticated user's cart. If the item already exists, its quantity is incremented."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Item added to cart successfully",
                    content = @Content(schema = @Schema(implementation = CartResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed or menu unavailable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Menu not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItemToCart(
            @Valid @RequestBody CartItemRequest request) {
        CartResponse response = cartService.addItemToCart(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "View cart",
            description = "Returns the authenticated user's cart with all items and totals"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Cart retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CartResponse.class))
            )
    })
    @GetMapping
    public ResponseEntity<CartResponse> getCart() {
        CartResponse response = cartService.getCart();
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Update cart item quantity",
            description = "Updates the quantity of a specific cart item"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Cart item updated successfully",
                    content = @Content(schema = @Schema(implementation = CartResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cart item not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponse> updateCartItemQuantity(
            @Parameter(description = "Cart item ID", example = "1")
            @PathVariable Long cartItemId,
            @Valid @RequestBody CartItemUpdateRequest request) {
        CartResponse response = cartService.updateCartItemQuantity(cartItemId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Remove item from cart",
            description = "Removes a specific item from the authenticated user's cart"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Cart item removed successfully",
                    content = @Content(schema = @Schema(implementation = CartResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cart item not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponse> removeCartItem(
            @Parameter(description = "Cart item ID", example = "1")
            @PathVariable Long cartItemId) {
        CartResponse response = cartService.removeCartItem(cartItemId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Clear cart",
            description = "Removes all items from the authenticated user's cart"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Cart cleared successfully",
                    content = @Content(schema = @Schema(implementation = CartResponse.class))
            )
    })
    @DeleteMapping
    public ResponseEntity<CartResponse> clearCart() {
        CartResponse response = cartService.clearCart();
        return ResponseEntity.ok(response);
    }

}
