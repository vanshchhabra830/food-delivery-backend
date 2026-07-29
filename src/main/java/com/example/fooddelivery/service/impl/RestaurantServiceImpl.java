package com.example.fooddelivery.service.impl;

import com.example.fooddelivery.dto.request.RestaurantRequest;
import com.example.fooddelivery.dto.response.RestaurantResponse;
import com.example.fooddelivery.entity.Restaurant;
import com.example.fooddelivery.exception.ResourceNotFoundException;
import com.example.fooddelivery.mapper.RestaurantMapper;
import com.example.fooddelivery.repository.RestaurantRepository;
import com.example.fooddelivery.service.RestaurantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public RestaurantServiceImpl(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    @Override
    public RestaurantResponse createRestaurant(RestaurantRequest request) {
        Restaurant restaurant = RestaurantMapper.toRestaurant(request);
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        log.info("Restaurant created successfully: {}", savedRestaurant.getName());
        return RestaurantMapper.toRestaurantResponse(savedRestaurant);
    }

    @Override
    public RestaurantResponse getRestaurantById(Long id) {
        Restaurant restaurant = findActiveRestaurantById(id);
        return RestaurantMapper.toRestaurantResponse(restaurant);
    }

    @Override
    public Page<RestaurantResponse> getAllRestaurants(Pageable pageable) {
        return restaurantRepository.findByActiveTrue(pageable)
                .map(RestaurantMapper::toRestaurantResponse);
    }

    @Override
    public RestaurantResponse updateRestaurant(Long id, RestaurantRequest request) {
        Restaurant restaurant = findActiveRestaurantById(id);

        restaurant.setName(request.getName());
        restaurant.setDescription(request.getDescription());
        restaurant.setAddress(request.getAddress());
        restaurant.setCity(request.getCity());
        restaurant.setCuisine(request.getCuisine());
        restaurant.setImageUrl(request.getImageUrl());

        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);
        log.info("Restaurant updated successfully: {}", updatedRestaurant.getName());
        return RestaurantMapper.toRestaurantResponse(updatedRestaurant);
    }

    @Override
    public void deleteRestaurant(Long id) {
        Restaurant restaurant = findActiveRestaurantById(id);
        restaurant.setActive(false);
        restaurantRepository.save(restaurant);
        log.info("Restaurant soft-deleted successfully: {}", restaurant.getName());
    }

    @Override
    public Page<RestaurantResponse> searchRestaurantsByName(String keyword, Pageable pageable) {
        log.info("Searching restaurants with keyword: {}", keyword);
        return restaurantRepository.findByNameContainingIgnoreCaseAndActiveTrue(keyword, pageable)
                .map(RestaurantMapper::toRestaurantResponse);
    }

    @Override
    public Page<RestaurantResponse> getRestaurantsByCuisine(String cuisine, Pageable pageable) {
        return restaurantRepository.findByCuisineIgnoreCaseAndActiveTrue(cuisine, pageable)
                .map(RestaurantMapper::toRestaurantResponse);
    }

    private Restaurant findActiveRestaurantById(Long id) {
        return restaurantRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + id));
    }

}
