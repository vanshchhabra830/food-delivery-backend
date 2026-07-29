package com.example.fooddelivery.service;

import com.example.fooddelivery.dto.request.RestaurantRequest;
import com.example.fooddelivery.dto.response.RestaurantResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RestaurantService {

    RestaurantResponse createRestaurant(RestaurantRequest request);

    RestaurantResponse getRestaurantById(Long id);

    Page<RestaurantResponse> getAllRestaurants(Pageable pageable);

    RestaurantResponse updateRestaurant(Long id, RestaurantRequest request);

    void deleteRestaurant(Long id);

    Page<RestaurantResponse> searchRestaurantsByName(String name, Pageable pageable);

    Page<RestaurantResponse> getRestaurantsByCuisine(String cuisine, Pageable pageable);

}
