package com.example.fooddelivery.repository;

import com.example.fooddelivery.entity.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    Page<Restaurant> findByActiveTrue(Pageable pageable);

    Page<Restaurant> findByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);

    Page<Restaurant> findByCuisineIgnoreCaseAndActiveTrue(String cuisine, Pageable pageable);

    Optional<Restaurant> findByIdAndActiveTrue(Long id);

}
