package com.example.fooddelivery.mapper;

import com.example.fooddelivery.dto.request.MenuRequest;
import com.example.fooddelivery.dto.response.MenuResponse;
import com.example.fooddelivery.entity.Menu;
import com.example.fooddelivery.entity.Restaurant;

public class MenuMapper {

    private MenuMapper() {
    }

    public static MenuResponse toMenuResponse(Menu menu) {
        if (menu == null) {
            return null;
        }

        return MenuResponse.builder()
                .id(menu.getId())
                .name(menu.getName())
                .description(menu.getDescription())
                .price(menu.getPrice())
                .category(menu.getCategory())
                .imageUrl(menu.getImageUrl())
                .available(menu.getAvailable())
                .restaurantId(menu.getRestaurant() != null ? menu.getRestaurant().getId() : null)
                .build();
    }

    public static Menu toMenu(MenuRequest request, Restaurant restaurant) {
        if (request == null) {
            return null;
        }

        return Menu.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(request.getCategory())
                .imageUrl(request.getImageUrl())
                .restaurant(restaurant)
                .build();
    }
}
