package com.example.fooddelivery.repository;

import com.example.fooddelivery.entity.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    Page<Restaurant> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Restaurant> findByCuisineIgnoreCase(String cuisine, Pageable pageable);

    Page<Restaurant> findByActiveTrue(Pageable pageable);

}
