package com.example.fooddelivery.controller;

import com.example.fooddelivery.dto.request.MenuRequest;
import com.example.fooddelivery.dto.response.MenuResponse;
import com.example.fooddelivery.exception.ErrorResponse;
import com.example.fooddelivery.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/menus")
@Tag(name = "Menus", description = "Menu management endpoints")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @Operation(
            summary = "Create a new menu item",
            description = "Creates a new menu item belonging to a specific restaurant"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Menu created successfully",
                    content = @Content(schema = @Schema(implementation = MenuResponse.class))
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
    @PostMapping("/restaurants/{restaurantId}")
    public ResponseEntity<MenuResponse> createMenu(
            @PathVariable Long restaurantId,
            @Valid @RequestBody MenuRequest request) {
        MenuResponse response = menuService.createMenu(restaurantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Get all menus",
            description = "Returns a paginated list of all menu items"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Menus retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<Page<MenuResponse>> getAllMenus(@PageableDefault(size = 10) Pageable pageable) {
        Page<MenuResponse> menus = menuService.getAllMenus(pageable);
        return ResponseEntity.ok(menus);
    }

    @Operation(
            summary = "Get menu by ID",
            description = "Returns a single menu item by its ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Menu found",
                    content = @Content(schema = @Schema(implementation = MenuResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Menu not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/{menuId}")
    public ResponseEntity<MenuResponse> getMenuById(
            @Parameter(description = "Menu ID", example = "1")
            @PathVariable Long menuId) {
        MenuResponse response = menuService.getMenuById(menuId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Update a menu",
            description = "Updates an existing menu item by its ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Menu updated successfully",
                    content = @Content(schema = @Schema(implementation = MenuResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Menu not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PutMapping("/{menuId}")
    public ResponseEntity<MenuResponse> updateMenu(
            @Parameter(description = "Menu ID", example = "1")
            @PathVariable Long menuId,
            @Valid @RequestBody MenuRequest request) {
        MenuResponse response = menuService.updateMenu(menuId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Delete a menu",
            description = "Deletes a menu item by its ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Menu deleted successfully"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Menu not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @DeleteMapping("/{menuId}")
    public ResponseEntity<Void> deleteMenu(
            @Parameter(description = "Menu ID", example = "1")
            @PathVariable Long menuId) {
        menuService.deleteMenu(menuId);
        return ResponseEntity.noContent().build();
    }

}
