package com.example.fooddelivery.controller;

import com.example.fooddelivery.dto.request.CartItemRequest;
import com.example.fooddelivery.dto.request.CartItemUpdateRequest;
import com.example.fooddelivery.entity.Menu;
import com.example.fooddelivery.entity.Restaurant;
import com.example.fooddelivery.entity.User;
import com.example.fooddelivery.enums.Role;
import com.example.fooddelivery.repository.CartItemRepository;
import com.example.fooddelivery.repository.CartRepository;
import com.example.fooddelivery.repository.MenuRepository;
import com.example.fooddelivery.repository.RestaurantRepository;
import com.example.fooddelivery.repository.UserRepository;
import com.example.fooddelivery.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CartControllerIntegrationTest {

    private static final String TEST_EMAIL = "cart-test@example.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String jwtToken;
    private Restaurant activeRestaurant;
    private Menu availableMenu;
    private Menu availableMenu2;

    @BeforeEach
    void setUp() {
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        menuRepository.deleteAll();
        restaurantRepository.deleteAll();

        if (!userRepository.existsByEmail(TEST_EMAIL)) {
            User testUser = User.builder()
                    .name("Cart Test User")
                    .email(TEST_EMAIL)
                    .password(passwordEncoder.encode("password123"))
                    .role(Role.CUSTOMER)
                    .build();
            userRepository.save(testUser);
        }

        jwtToken = jwtTokenProvider.generateToken(TEST_EMAIL, "CUSTOMER");

        activeRestaurant = restaurantRepository.save(Restaurant.builder()
                .name("Test Restaurant")
                .description("A test restaurant")
                .address("123 Test Street")
                .city("TestCity")
                .cuisine("TestCuisine")
                .active(true)
                .build());

        availableMenu = menuRepository.save(Menu.builder()
                .restaurant(activeRestaurant)
                .name("Burger")
                .description("Delicious burger")
                .price(10.0)
                .category("Fast Food")
                .available(true)
                .build());

        availableMenu2 = menuRepository.save(Menu.builder()
                .restaurant(activeRestaurant)
                .name("Pizza")
                .description("Cheesy pizza")
                .price(15.0)
                .category("Fast Food")
                .available(true)
                .build());
    }

    @Test
    @DisplayName("POST /api/v1/cart/items - Should add item to cart")
    void addItemToCart_success() throws Exception {
        CartItemRequest request = new CartItemRequest(availableMenu.getId(), 2);

        mockMvc.perform(post("/api/v1/cart/items")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].menuName", is("Burger")))
                .andExpect(jsonPath("$.items[0].quantity", is(2)))
                .andExpect(jsonPath("$.items[0].subtotal", is(20.0)))
                .andExpect(jsonPath("$.totalItems", is(2)))
                .andExpect(jsonPath("$.totalAmount", is(20.0)));
    }

    @Test
    @DisplayName("POST /api/v1/cart/items - Should increment quantity for duplicate item")
    void addItemToCart_duplicate_incrementsQuantity() throws Exception {
        CartItemRequest request = new CartItemRequest(availableMenu.getId(), 2);

        mockMvc.perform(post("/api/v1/cart/items")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/cart/items")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].quantity", is(4)))
                .andExpect(jsonPath("$.items[0].subtotal", is(40.0)))
                .andExpect(jsonPath("$.totalItems", is(4)))
                .andExpect(jsonPath("$.totalAmount", is(40.0)));
    }

    @Test
    @DisplayName("GET /api/v1/cart - Should return cart with items")
    void getCart_success() throws Exception {
        CartItemRequest request = new CartItemRequest(availableMenu.getId(), 1);
        mockMvc.perform(post("/api/v1/cart/items")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/cart")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.totalItems", is(1)))
                .andExpect(jsonPath("$.totalAmount", is(10.0)));
    }

    @Test
    @DisplayName("GET /api/v1/cart - Should return empty cart if no cart exists")
    void getCart_empty() throws Exception {
        mockMvc.perform(get("/api/v1/cart")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.totalItems", is(0)))
                .andExpect(jsonPath("$.totalAmount", is(0.0)));
    }

    @Test
    @DisplayName("PUT /api/v1/cart/items/{cartItemId} - Should update quantity")
    void updateCartItemQuantity_success() throws Exception {
        CartItemRequest addRequest = new CartItemRequest(availableMenu.getId(), 1);
        String addResponse = mockMvc.perform(post("/api/v1/cart/items")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long cartItemId = objectMapper.readTree(addResponse).get("items").get(0).get("id").asLong();

        CartItemUpdateRequest updateRequest = new CartItemUpdateRequest(5);
        mockMvc.perform(put("/api/v1/cart/items/" + cartItemId)
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity", is(5)))
                .andExpect(jsonPath("$.items[0].subtotal", is(50.0)))
                .andExpect(jsonPath("$.totalItems", is(5)))
                .andExpect(jsonPath("$.totalAmount", is(50.0)));
    }

    @Test
    @DisplayName("DELETE /api/v1/cart/items/{cartItemId} - Should remove item")
    void removeCartItem_success() throws Exception {
        // Add two different items
        mockMvc.perform(post("/api/v1/cart/items")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CartItemRequest(availableMenu.getId(), 1))))
                .andExpect(status().isOk());

        String addResponse = mockMvc.perform(post("/api/v1/cart/items")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CartItemRequest(availableMenu2.getId(), 2))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Remove the first item (Burger)
        Long firstItemId = objectMapper.readTree(addResponse).get("items").get(0).get("id").asLong();

        mockMvc.perform(delete("/api/v1/cart/items/" + firstItemId)
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.totalItems", is(2)))
                .andExpect(jsonPath("$.totalAmount", is(30.0)));
    }

    @Test
    @DisplayName("DELETE /api/v1/cart - Should clear all items")
    void clearCart_success() throws Exception {
        mockMvc.perform(post("/api/v1/cart/items")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CartItemRequest(availableMenu.getId(), 2))))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/cart")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.totalItems", is(0)))
                .andExpect(jsonPath("$.totalAmount", is(0.0)));
    }

    @Test
    @DisplayName("POST /api/v1/cart/items - Should return 404 for invalid menu ID")
    void addItemToCart_invalidMenu_returns404() throws Exception {
        CartItemRequest request = new CartItemRequest(99999L, 1);

        mockMvc.perform(post("/api/v1/cart/items")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/cart/items - Should return 400 for unavailable menu")
    void addItemToCart_unavailableMenu_returns400() throws Exception {
        Menu unavailableMenu = menuRepository.save(Menu.builder()
                .restaurant(activeRestaurant)
                .name("Unavailable Item")
                .description("Not available")
                .price(20.0)
                .category("Food")
                .available(false)
                .build());

        CartItemRequest request = new CartItemRequest(unavailableMenu.getId(), 1);

        mockMvc.perform(post("/api/v1/cart/items")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/cart/items - Should return 400 for menu from inactive restaurant")
    void addItemToCart_inactiveRestaurant_returns400() throws Exception {
        Restaurant inactiveRestaurant = restaurantRepository.save(Restaurant.builder()
                .name("Closed Restaurant")
                .description("Closed")
                .address("456 Closed St")
                .city("City")
                .cuisine("Cuisine")
                .active(false)
                .build());

        Menu menuFromInactive = menuRepository.save(Menu.builder()
                .restaurant(inactiveRestaurant)
                .name("Closed Menu Item")
                .description("From closed restaurant")
                .price(25.0)
                .category("Food")
                .available(true)
                .build());

        CartItemRequest request = new CartItemRequest(menuFromInactive.getId(), 1);

        mockMvc.perform(post("/api/v1/cart/items")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/v1/cart/items/{cartItemId} - Should return 400 for quantity 0")
    void updateCartItemQuantity_zeroQuantity_returns400() throws Exception {
        String addResponse = mockMvc.perform(post("/api/v1/cart/items")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CartItemRequest(availableMenu.getId(), 1))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long cartItemId = objectMapper.readTree(addResponse).get("items").get(0).get("id").asLong();

        String invalidJson = "{\"quantity\": 0}";

        mockMvc.perform(put("/api/v1/cart/items/" + cartItemId)
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

}
