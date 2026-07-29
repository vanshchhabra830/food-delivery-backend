package com.example.fooddelivery.mapper;

import com.example.fooddelivery.dto.response.CartItemResponse;
import com.example.fooddelivery.dto.response.CartResponse;
import com.example.fooddelivery.entity.Cart;
import com.example.fooddelivery.entity.CartItem;

import java.util.Collections;
import java.util.List;

public class CartMapper {

    private CartMapper() {
    }

    public static CartResponse toCartResponse(Cart cart) {
        if (cart == null) {
            return null;
        }

        List<CartItemResponse> items = cart.getCartItems() != null
                ? cart.getCartItems().stream()
                        .map(CartMapper::toCartItemResponse)
                        .toList()
                : Collections.emptyList();

        int totalItems = cart.getCartItems() != null
                ? cart.getCartItems().stream()
                        .mapToInt(CartItem::getQuantity)
                        .sum()
                : 0;

        return CartResponse.builder()
                .id(cart.getId())
                .items(items)
                .totalItems(totalItems)
                .totalAmount(cart.getTotalAmount())
                .build();
    }

    public static CartItemResponse toCartItemResponse(CartItem cartItem) {
        if (cartItem == null) {
            return null;
        }

        return CartItemResponse.builder()
                .id(cartItem.getId())
                .menuId(cartItem.getMenu() != null ? cartItem.getMenu().getId() : null)
                .menuName(cartItem.getMenu() != null ? cartItem.getMenu().getName() : null)
                .menuPrice(cartItem.getMenu() != null ? cartItem.getMenu().getPrice() : null)
                .quantity(cartItem.getQuantity())
                .subtotal(cartItem.getSubtotal())
                .build();
    }

}
