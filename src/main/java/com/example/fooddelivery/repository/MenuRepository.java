package com.example.fooddelivery.repository;

import com.example.fooddelivery.entity.Menu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    Page<Menu> findByAvailableTrueAndRestaurantActiveTrue(Pageable pageable);

    Page<Menu> findByCategoryIgnoreCaseAndAvailableTrueAndRestaurantActiveTrue(String category, Pageable pageable);

    Page<Menu> findByPriceBetweenAndAvailableTrueAndRestaurantActiveTrue(Double minPrice, Double maxPrice, Pageable pageable);

    Page<Menu> findByNameContainingIgnoreCaseAndAvailableTrueAndRestaurantActiveTrue(String keyword, Pageable pageable);

}
