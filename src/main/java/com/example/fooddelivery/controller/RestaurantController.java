package com.example.fooddelivery.controller;

import com.example.fooddelivery.dto.request.RestaurantRequest;
import com.example.fooddelivery.dto.response.RestaurantResponse;
import com.example.fooddelivery.exception.ErrorResponse;
import com.example.fooddelivery.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/restaurants")
@Tag(name = "Restaurants", description = "Restaurant management endpoints")
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @Operation(
            summary = "Create a new restaurant",
            description = "Creates a new restaurant listing"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Restaurant created successfully",
                    content = @Content(schema = @Schema(implementation = RestaurantResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping
    public ResponseEntity<RestaurantResponse> createRestaurant(@Valid @RequestBody RestaurantRequest request) {
        RestaurantResponse response = restaurantService.createRestaurant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Get all restaurants",
            description = "Returns a paginated list of all active restaurants"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Restaurants retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<Page<RestaurantResponse>> getAllRestaurants(@PageableDefault(size = 10) Pageable pageable) {
        Page<RestaurantResponse> restaurants = restaurantService.getAllRestaurants(pageable);
        return ResponseEntity.ok(restaurants);
    }

    @Operation(
            summary = "Get restaurant by ID",
            description = "Returns a single restaurant by its ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Restaurant found",
                    content = @Content(schema = @Schema(implementation = RestaurantResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Restaurant not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponse> getRestaurantById(@PathVariable Long id) {
        RestaurantResponse response = restaurantService.getRestaurantById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Update a restaurant",
            description = "Updates an existing restaurant by its ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Restaurant updated successfully",
                    content = @Content(schema = @Schema(implementation = RestaurantResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Restaurant not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<RestaurantResponse> updateRestaurant(@PathVariable Long id,
                                                               @Valid @RequestBody RestaurantRequest request) {
        RestaurantResponse response = restaurantService.updateRestaurant(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Delete a restaurant",
            description = "Deletes a restaurant by its ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Restaurant deleted successfully"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Restaurant not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRestaurant(@PathVariable Long id) {
        restaurantService.deleteRestaurant(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Search restaurants by name",
            description = "Returns a paginated list of restaurants matching the search query"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search results retrieved successfully")
    })
    @GetMapping("/search")
    public ResponseEntity<Page<RestaurantResponse>> searchRestaurants(
            @RequestParam String name,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<RestaurantResponse> restaurants = restaurantService.searchRestaurantsByName(name, pageable);
        return ResponseEntity.ok(restaurants);
    }

    @Operation(
            summary = "Get restaurants by cuisine",
            description = "Returns a paginated list of restaurants filtered by cuisine type"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Restaurants retrieved successfully")
    })
    @GetMapping("/cuisine")
    public ResponseEntity<Page<RestaurantResponse>> getRestaurantsByCuisine(
            @RequestParam String type,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<RestaurantResponse> restaurants = restaurantService.getRestaurantsByCuisine(type, pageable);
        return ResponseEntity.ok(restaurants);
    }

}
