package com.example.fooddelivery.controller;

import com.example.fooddelivery.dto.request.RestaurantRequest;
import com.example.fooddelivery.entity.Restaurant;
import com.example.fooddelivery.entity.User;
import com.example.fooddelivery.enums.Role;
import com.example.fooddelivery.repository.CartItemRepository;
import com.example.fooddelivery.repository.CartRepository;
import com.example.fooddelivery.repository.RestaurantRepository;
import com.example.fooddelivery.repository.UserRepository;
import com.example.fooddelivery.repository.MenuRepository;
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
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RestaurantControllerIntegrationTest {

    private static final String TEST_EMAIL = "restaurant-test@example.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String jwtToken;

    @BeforeEach
    void setUp() {
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        menuRepository.deleteAll();
        restaurantRepository.deleteAll();

        if (!userRepository.existsByEmail(TEST_EMAIL)) {
            User testUser = User.builder()
                    .name("Test Owner")
                    .email(TEST_EMAIL)
                    .password(passwordEncoder.encode("password123"))
                    .role(Role.RESTAURANT_OWNER)
                    .build();
            userRepository.save(testUser);
        }

        jwtToken = jwtTokenProvider.generateToken(TEST_EMAIL, "RESTAURANT_OWNER");
    }

    private RestaurantRequest createValidRequest() {
        return new RestaurantRequest(
                "Pizza Palace",
                "Best pizza in town",
                "123 Main Street",
                "Mumbai",
                "Italian",
                "https://example.com/pizza.jpg");
    }

    private Restaurant seedRestaurant(String name, String cuisine, boolean active) {
        Restaurant restaurant = Restaurant.builder()
                .name(name)
                .description("A great place")
                .address("123 Street")
                .city("Mumbai")
                .cuisine(cuisine)
                .active(active)
                .build();
        return restaurantRepository.save(restaurant);
    }

    @Test
    @DisplayName("POST /api/v1/restaurants - Should create restaurant and return 201")
    void createRestaurant_success() throws Exception {
        RestaurantRequest request = createValidRequest();

        mockMvc.perform(post("/api/v1/restaurants")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.name", is("Pizza Palace")))
                .andExpect(jsonPath("$.cuisine", is("Italian")))
                .andExpect(jsonPath("$.active", is(true)))
                .andExpect(jsonPath("$.rating", is(0.0)));
    }

    @Test
    @DisplayName("POST /api/v1/restaurants - Should return 400 for validation failure")
    void createRestaurant_validationFailure() throws Exception {
        String invalidJson = "{\"name\":\"\",\"address\":\"\",\"city\":\"\",\"cuisine\":\"\"}";

        mockMvc.perform(post("/api/v1/restaurants")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is(notNullValue())));
    }

    @Test
    @DisplayName("PUT /api/v1/restaurants/{id} - Should update restaurant and return 200")
    void updateRestaurant_success() throws Exception {
        Restaurant restaurant = seedRestaurant("Old Name", "Chinese", true);
        RestaurantRequest updateRequest = new RestaurantRequest(
                "New Name", "Updated desc", "456 Avenue", "Delhi", "Indian", "https://example.com/new.jpg");

        mockMvc.perform(put("/api/v1/restaurants/" + restaurant.getId())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("New Name")))
                .andExpect(jsonPath("$.cuisine", is("Indian")));
    }

    @Test
    @DisplayName("DELETE /api/v1/restaurants/{id} - Should soft delete and return 204")
    void deleteRestaurant_softDelete() throws Exception {
        Restaurant restaurant = seedRestaurant("To Delete", "Italian", true);

        mockMvc.perform(delete("/api/v1/restaurants/" + restaurant.getId())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());

        // Verify it's soft-deleted (still in DB but inactive)
        Restaurant deleted = restaurantRepository.findById(restaurant.getId()).orElseThrow();
        assertFalse(deleted.getActive());
    }

    @Test
    @DisplayName("GET /api/v1/restaurants - Soft-deleted restaurants should not appear")
    void getAllRestaurants_excludesSoftDeleted() throws Exception {
        seedRestaurant("Active Restaurant", "Italian", true);
        seedRestaurant("Deleted Restaurant", "Chinese", false);

        mockMvc.perform(get("/api/v1/restaurants")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("Active Restaurant")));
    }

    @Test
    @DisplayName("GET /api/v1/restaurants/{id} - Soft-deleted restaurant returns 404")
    void getRestaurantById_softDeleted_returns404() throws Exception {
        Restaurant restaurant = seedRestaurant("Deleted", "Italian", false);

        mockMvc.perform(get("/api/v1/restaurants/" + restaurant.getId())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/restaurants/search - Should search by keyword (case-insensitive)")
    void searchRestaurants_byKeyword() throws Exception {
        seedRestaurant("Pizza Palace", "Italian", true);
        seedRestaurant("Burger Barn", "American", true);
        seedRestaurant("Pizza Hut", "Italian", true);

        mockMvc.perform(get("/api/v1/restaurants/search")
                .param("keyword", "pizza")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    @DisplayName("GET /api/v1/restaurants/search - Should exclude soft-deleted from search")
    void searchRestaurants_excludesSoftDeleted() throws Exception {
        seedRestaurant("Pizza Palace", "Italian", true);
        seedRestaurant("Pizza Deleted", "Italian", false);

        mockMvc.perform(get("/api/v1/restaurants/search")
                .param("keyword", "pizza")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("Pizza Palace")));
    }

    @Test
    @DisplayName("GET /api/v1/restaurants/cuisine - Should filter by cuisine (active only)")
    void getRestaurantsByCuisine_activeOnly() throws Exception {
        seedRestaurant("Active Italian", "Italian", true);
        seedRestaurant("Deleted Italian", "Italian", false);
        seedRestaurant("Active Chinese", "Chinese", true);

        mockMvc.perform(get("/api/v1/restaurants/cuisine")
                .param("type", "Italian")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("Active Italian")));
    }

    @Test
    @DisplayName("GET /api/v1/restaurants - Should support pagination and sorting")
    void getAllRestaurants_paginationAndSorting() throws Exception {
        seedRestaurant("Charlie's", "Italian", true);
        seedRestaurant("Alpha Bites", "Chinese", true);
        seedRestaurant("Bravo Burgers", "American", true);

        mockMvc.perform(get("/api/v1/restaurants")
                .param("page", "0")
                .param("size", "2")
                .param("sortBy", "name")
                .param("direction", "asc")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].name", is("Alpha Bites")))
                .andExpect(jsonPath("$.content[1].name", is("Bravo Burgers")))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.totalPages", is(2)));
    }

    @Test
    @DisplayName("POST /api/v1/restaurants - Should return 400 for invalid image URL")
    void createRestaurant_invalidUrl() throws Exception {
        RestaurantRequest request = new RestaurantRequest(
                "Test Restaurant", "Desc", "Address", "City", "Cuisine", "not-a-url");

        mockMvc.perform(post("/api/v1/restaurants")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    @DisplayName("GET /api/v1/restaurants - Should return 403 without JWT token")
    void getAllRestaurants_noAuth_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/restaurants"))
                .andExpect(status().isForbidden());
    }

}
