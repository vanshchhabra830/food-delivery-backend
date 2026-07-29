package com.example.fooddelivery.repository;

import com.example.fooddelivery.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartIdAndMenuId(Long cartId, Long menuId);

}
