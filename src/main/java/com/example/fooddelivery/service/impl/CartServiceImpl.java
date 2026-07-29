package com.example.fooddelivery.service.impl;

import com.example.fooddelivery.dto.request.CartItemRequest;
import com.example.fooddelivery.dto.request.CartItemUpdateRequest;
import com.example.fooddelivery.dto.response.CartResponse;
import com.example.fooddelivery.entity.Cart;
import com.example.fooddelivery.entity.CartItem;
import com.example.fooddelivery.entity.Menu;
import com.example.fooddelivery.entity.User;
import com.example.fooddelivery.exception.ResourceNotFoundException;
import com.example.fooddelivery.mapper.CartMapper;
import com.example.fooddelivery.repository.CartItemRepository;
import com.example.fooddelivery.repository.CartRepository;
import com.example.fooddelivery.repository.MenuRepository;
import com.example.fooddelivery.repository.UserRepository;
import com.example.fooddelivery.service.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MenuRepository menuRepository;
    private final UserRepository userRepository;

    public CartServiceImpl(CartRepository cartRepository,
                           CartItemRepository cartItemRepository,
                           MenuRepository menuRepository,
                           UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.menuRepository = menuRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public CartResponse addItemToCart(CartItemRequest request) {
        User user = getCurrentUser();
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = Cart.builder().user(user).build();
                    return cartRepository.save(newCart);
                });

        Menu menu = menuRepository.findById(request.getMenuId())
                .orElseThrow(() -> new ResourceNotFoundException("Menu not found with id: " + request.getMenuId()));

        if (!menu.getAvailable()) {
            log.warn("Attempt to add unavailable menu item: {}", menu.getName());
            throw new IllegalArgumentException("Menu item is not available: " + menu.getName());
        }

        if (!menu.getRestaurant().getActive()) {
            log.warn("Attempt to add menu item from inactive restaurant: {}", menu.getRestaurant().getName());
            throw new IllegalArgumentException("Restaurant is not active: " + menu.getRestaurant().getName());
        }

        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndMenuId(cart.getId(), menu.getId());

        if (existingItem.isPresent()) {
            CartItem cartItem = existingItem.get();
            int newQuantity = cartItem.getQuantity() + request.getQuantity();
            if (newQuantity > 99) {
                log.warn("Quantity exceeds maximum limit for menu item: {}", menu.getName());
                throw new IllegalArgumentException("Quantity must not exceed 99");
            }
            cartItem.setQuantity(newQuantity);
            cartItem.setSubtotal(menu.getPrice() * newQuantity);
            log.info("Updated quantity for menu item '{}' in cart to {}", menu.getName(), newQuantity);
        } else {
            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .menu(menu)
                    .quantity(request.getQuantity())
                    .subtotal(menu.getPrice() * request.getQuantity())
                    .build();
            cart.getCartItems().add(cartItem);
            log.info("Added menu item '{}' to cart with quantity {}", menu.getName(), request.getQuantity());
        }

        recalculateCartTotal(cart);
        Cart savedCart = cartRepository.save(cart);
        return CartMapper.toCartResponse(savedCart);
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart() {
        User user = getCurrentUser();
        Cart cart = cartRepository.findByUserId(user.getId()).orElse(null);

        if (cart == null) {
            return CartResponse.builder()
                    .items(java.util.Collections.emptyList())
                    .totalItems(0)
                    .totalAmount(0.0)
                    .build();
        }

        return CartMapper.toCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse updateCartItemQuantity(Long cartItemId, CartItemUpdateRequest request) {
        User user = getCurrentUser();
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            log.warn("Cart item {} does not belong to user's cart", cartItemId);
            throw new IllegalArgumentException("Cart item does not belong to your cart");
        }

        cartItem.setQuantity(request.getQuantity());
        cartItem.setSubtotal(cartItem.getMenu().getPrice() * request.getQuantity());
        log.info("Updated cart item {} quantity to {}", cartItemId, request.getQuantity());

        recalculateCartTotal(cart);
        Cart savedCart = cartRepository.save(cart);
        return CartMapper.toCartResponse(savedCart);
    }

    @Override
    @Transactional
    public CartResponse removeCartItem(Long cartItemId) {
        User user = getCurrentUser();
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            log.warn("Cart item {} does not belong to user's cart", cartItemId);
            throw new IllegalArgumentException("Cart item does not belong to your cart");
        }

        cart.getCartItems().remove(cartItem);
        log.info("Removed cart item {} from cart", cartItemId);

        recalculateCartTotal(cart);
        Cart savedCart = cartRepository.save(cart);
        return CartMapper.toCartResponse(savedCart);
    }

    @Override
    @Transactional
    public CartResponse clearCart() {
        User user = getCurrentUser();
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        cart.getCartItems().clear();
        cart.setTotalAmount(0.0);
        log.info("Cleared cart for user: {}", user.getEmail());

        Cart savedCart = cartRepository.save(cart);
        return CartMapper.toCartResponse(savedCart);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void recalculateCartTotal(Cart cart) {
        double total = cart.getCartItems().stream()
                .mapToDouble(CartItem::getSubtotal)
                .sum();
        cart.setTotalAmount(total);
    }

}
