package com.example.fooddelivery.repository;

import com.example.fooddelivery.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    
    // Repository methods for current entity relationship can be added here
    // No service methods implemented yet as per instructions

}
