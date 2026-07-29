package com.example.fooddelivery.controller;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MenuControllerIntegrationTest {

    private static final String TEST_EMAIL = "menu-test@example.com";

    @Autowired
    private MockMvc mockMvc;

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
    private Restaurant activeRestaurant;
    private Restaurant inactiveRestaurant;

    @BeforeEach
    void setUp() {
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        menuRepository.deleteAll();
        restaurantRepository.deleteAll();

        if (!userRepository.existsByEmail(TEST_EMAIL)) {
            User testUser = User.builder()
                    .name("Test User")
                    .email(TEST_EMAIL)
                    .password(passwordEncoder.encode("password123"))
                    .role(Role.CUSTOMER)
                    .build();
            userRepository.save(testUser);
        }

        jwtToken = jwtTokenProvider.generateToken(TEST_EMAIL, "CUSTOMER");

        activeRestaurant = seedRestaurant("Active Rest", true);
        inactiveRestaurant = seedRestaurant("Inactive Rest", false);
    }

    private Restaurant seedRestaurant(String name, boolean active) {
        Restaurant restaurant = Restaurant.builder()
                .name(name)
                .description("Desc")
                .address("Address")
                .city("City")
                .cuisine("Cuisine")
                .active(active)
                .build();
        return restaurantRepository.save(restaurant);
    }

    private Menu seedMenu(Restaurant restaurant, String name, String category, Double price, boolean available) {
        Menu menu = Menu.builder()
                .restaurant(restaurant)
                .name(name)
                .description("Desc")
                .price(price)
                .category(category)
                .available(available)
                .build();
        return menuRepository.save(menu);
    }

    @Test
    @DisplayName("GET /api/v1/menus/available - Should return only available menus from active restaurants")
    void getAvailableMenus() throws Exception {
        seedMenu(activeRestaurant, "Burger", "Fast Food", 100.0, true);
        seedMenu(activeRestaurant, "Pizza", "Fast Food", 150.0, false); // Unavailable
        seedMenu(inactiveRestaurant, "Pasta", "Italian", 200.0, true); // Inactive restaurant

        mockMvc.perform(get("/api/v1/menus/available")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("Burger")));
    }

    @Test
    @DisplayName("GET /api/v1/menus/available - Should support pagination")
    void getAvailableMenus_pagination() throws Exception {
        for (int i = 0; i < 5; i++) {
            seedMenu(activeRestaurant, "Item " + i, "Cat", 10.0, true);
        }

        mockMvc.perform(get("/api/v1/menus/available")
                .param("page", "0")
                .param("size", "2")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", is(5)));
    }

    @Test
    @DisplayName("GET /api/v1/menus/category/{category} - Should filter by category (case-insensitive)")
    void getMenusByCategory() throws Exception {
        seedMenu(activeRestaurant, "Margherita", "PIZZA", 100.0, true);
        seedMenu(activeRestaurant, "Pepperoni", "pizza", 120.0, true);
        seedMenu(activeRestaurant, "Burger", "Fast Food", 80.0, true);
        seedMenu(activeRestaurant, "Inactive Pizza", "pizza", 150.0, false);
        seedMenu(inactiveRestaurant, "Pasta", "pizza", 200.0, true);

        mockMvc.perform(get("/api/v1/menus/category/pIzZa")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    @DisplayName("GET /api/v1/menus/category/{category} - Blank category should return 400")
    void getMenusByCategory_blank() throws Exception {
        mockMvc.perform(get("/api/v1/menus/category/ ")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/menus/price - Should filter by price range")
    void getMenusByPriceRange() throws Exception {
        seedMenu(activeRestaurant, "Cheap", "Food", 50.0, true);
        seedMenu(activeRestaurant, "Medium", "Food", 150.0, true);
        seedMenu(activeRestaurant, "Expensive", "Food", 300.0, true);
        seedMenu(activeRestaurant, "Medium Unavail", "Food", 150.0, false);
        seedMenu(inactiveRestaurant, "Medium Inactive", "Food", 150.0, true);

        mockMvc.perform(get("/api/v1/menus/price")
                .param("minPrice", "100.0")
                .param("maxPrice", "200.0")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("Medium")));
    }

    @Test
    @DisplayName("GET /api/v1/menus/price - Negative price should return 400")
    void getMenusByPriceRange_negative() throws Exception {
        mockMvc.perform(get("/api/v1/menus/price")
                .param("minPrice", "-10.0")
                .param("maxPrice", "50.0")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/menus/price - minPrice > maxPrice should return 400")
    void getMenusByPriceRange_invalidRange() throws Exception {
        mockMvc.perform(get("/api/v1/menus/price")
                .param("minPrice", "100.0")
                .param("maxPrice", "50.0")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/menus/search - Should search by keyword (partial, case-insensitive)")
    void searchMenus() throws Exception {
        seedMenu(activeRestaurant, "Spicy Chicken Burger", "Fast Food", 150.0, true);
        seedMenu(activeRestaurant, "Veggie BURGER", "Fast Food", 120.0, true);
        seedMenu(activeRestaurant, "Pizza", "Fast Food", 200.0, true);
        seedMenu(activeRestaurant, "Beef Burger", "Fast Food", 180.0, false); // unavailable
        seedMenu(inactiveRestaurant, "Fish Burger", "Fast Food", 160.0, true); // inactive

        mockMvc.perform(get("/api/v1/menus/search")
                .param("keyword", "  burger  ")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    @DisplayName("GET /api/v1/menus/search - Blank keyword should return 400")
    void searchMenus_blank() throws Exception {
        mockMvc.perform(get("/api/v1/menus/search")
                .param("keyword", "   ")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isBadRequest());
    }
}
