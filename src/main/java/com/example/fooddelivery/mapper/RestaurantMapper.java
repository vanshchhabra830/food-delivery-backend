package com.example.fooddelivery.mapper;

import com.example.fooddelivery.dto.request.RestaurantRequest;
import com.example.fooddelivery.dto.response.RestaurantResponse;
import com.example.fooddelivery.entity.Restaurant;

public class RestaurantMapper {

    private RestaurantMapper() {
    }

    public static RestaurantResponse toRestaurantResponse(Restaurant restaurant) {
        return RestaurantResponse.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .description(restaurant.getDescription())
                .address(restaurant.getAddress())
                .city(restaurant.getCity())
                .cuisine(restaurant.getCuisine())
                .imageUrl(restaurant.getImageUrl())
                .rating(restaurant.getRating())
                .active(restaurant.getActive())
                .build();
    }

    public static Restaurant toRestaurant(RestaurantRequest request) {
        return Restaurant.builder()
                .name(request.getName())
                .description(request.getDescription())
                .address(request.getAddress())
                .city(request.getCity())
                .cuisine(request.getCuisine())
                .imageUrl(request.getImageUrl())
                .build();
    }

}
