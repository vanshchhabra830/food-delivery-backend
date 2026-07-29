package com.example.fooddelivery.service;

import com.example.fooddelivery.dto.request.CartItemRequest;
import com.example.fooddelivery.dto.request.CartItemUpdateRequest;
import com.example.fooddelivery.dto.response.CartResponse;

public interface CartService {

    CartResponse addItemToCart(CartItemRequest request);

    CartResponse getCart();

    CartResponse updateCartItemQuantity(Long cartItemId, CartItemUpdateRequest request);

    CartResponse removeCartItem(Long cartItemId);

    CartResponse clearCart();

}
