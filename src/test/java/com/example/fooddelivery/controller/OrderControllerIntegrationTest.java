package com.example.fooddelivery.controller;

import com.example.fooddelivery.dto.request.CartItemRequest;
import com.example.fooddelivery.dto.request.OrderRequest;
import com.example.fooddelivery.entity.Address;
import com.example.fooddelivery.entity.Cart;
import com.example.fooddelivery.entity.Menu;
import com.example.fooddelivery.entity.Order;
import com.example.fooddelivery.entity.Restaurant;
import com.example.fooddelivery.entity.User;
import com.example.fooddelivery.enums.OrderStatus;
import com.example.fooddelivery.enums.Role;
import com.example.fooddelivery.repository.AddressRepository;
import com.example.fooddelivery.repository.CartItemRepository;
import com.example.fooddelivery.repository.CartRepository;
import com.example.fooddelivery.repository.MenuRepository;
import com.example.fooddelivery.repository.OrderItemRepository;
import com.example.fooddelivery.repository.OrderRepository;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerIntegrationTest {

    private static final String TEST_EMAIL = "order-test@example.com";
    private static final String OTHER_EMAIL = "order-other@example.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private AddressRepository addressRepository;

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
    private String otherJwtToken;
    private User testUser;
    private User otherUser;
    private Restaurant activeRestaurant;
    private Menu availableMenu;
    private Address testAddress;

    @BeforeEach
    void setUp() {
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        addressRepository.deleteAll();
        menuRepository.deleteAll();
        restaurantRepository.deleteAll();

        testUser = userRepository.findByEmail(TEST_EMAIL).orElseGet(() -> userRepository.save(User.builder()
                .name("Order Test User")
                .email(TEST_EMAIL)
                .password(passwordEncoder.encode("password123"))
                .role(Role.CUSTOMER)
                .build()));

        otherUser = userRepository.findByEmail(OTHER_EMAIL).orElseGet(() -> userRepository.save(User.builder()
                .name("Order Other User")
                .email(OTHER_EMAIL)
                .password(passwordEncoder.encode("password123"))
                .role(Role.CUSTOMER)
                .build()));

        jwtToken = jwtTokenProvider.generateToken(TEST_EMAIL, "CUSTOMER");
        otherJwtToken = jwtTokenProvider.generateToken(OTHER_EMAIL, "CUSTOMER");

        activeRestaurant = restaurantRepository.save(Restaurant.builder()
                .name("Order Test Restaurant")
                .description("Test")
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

        testAddress = addressRepository.save(Address.builder()
                .user(testUser)
                .fullName("John Doe")
                .phoneNumber("1234567890")
                .addressLine1("123 Main St")
                .addressLine2("Apt 4B")
                .landmark("Near Park")
                .city("New York")
                .state("NY")
                .postalCode("10001")
                .country("USA")
                .isDefault(true)
                .build());
    }

    private void addItemToCart() throws Exception {
        CartItemRequest cartRequest = new CartItemRequest(availableMenu.getId(), 2);
        mockMvc.perform(post("/api/v1/cart/items")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartRequest)))
                .andExpect(status().isOk());
    }

    private Long createOrderAndReturnId() throws Exception {
        addItemToCart();
        OrderRequest orderRequest = new OrderRequest(testAddress.getId());
        String response = mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    @Test
    @DisplayName("POST /api/v1/orders - Should create order successfully")
    void createOrder_success() throws Exception {
        addItemToCart();
        OrderRequest orderRequest = new OrderRequest(testAddress.getId());

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status", is("PLACED")))
                .andExpect(jsonPath("$.totalAmount", is(20.0)))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].menuName", is("Burger")))
                .andExpect(jsonPath("$.items[0].quantity", is(2)))
                .andExpect(jsonPath("$.deliveryAddress").exists());
    }

    @Test
    @DisplayName("POST /api/v1/orders - Should fail when cart is empty")
    void createOrder_emptyCart_returns400() throws Exception {
        cartRepository.save(Cart.builder().user(testUser).totalAmount(0.0).build());

        OrderRequest orderRequest = new OrderRequest(testAddress.getId());

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Cart is empty")));
    }

    @Test
    @DisplayName("POST /api/v1/orders - Should fail for invalid address ID")
    void createOrder_invalidAddress_returns404() throws Exception {
        addItemToCart();
        OrderRequest orderRequest = new OrderRequest(99999L);

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/orders - Should fail when using another user's address")
    void createOrder_otherUserAddress_returns404() throws Exception {
        addItemToCart();

        Address otherAddress = addressRepository.save(Address.builder()
                .user(otherUser)
                .fullName("Other User")
                .phoneNumber("9876543210")
                .addressLine1("456 Other St")
                .city("Boston")
                .state("MA")
                .postalCode("02101")
                .country("USA")
                .isDefault(true)
                .build());

        OrderRequest orderRequest = new OrderRequest(otherAddress.getId());

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/orders - Should fail when menu becomes unavailable")
    void createOrder_unavailableMenu_returns400() throws Exception {
        addItemToCart();

        availableMenu.setAvailable(false);
        menuRepository.save(availableMenu);

        OrderRequest orderRequest = new OrderRequest(testAddress.getId());

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.containsString("not available")));
    }

    @Test
    @DisplayName("POST /api/v1/orders - Should fail when restaurant is inactive")
    void createOrder_inactiveRestaurant_returns400() throws Exception {
        addItemToCart();

        activeRestaurant.setActive(false);
        restaurantRepository.save(activeRestaurant);

        OrderRequest orderRequest = new OrderRequest(testAddress.getId());

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.containsString("not active")));
    }

    @Test
    @DisplayName("GET /api/v1/orders - Should return order history newest first")
    void getMyOrders_success() throws Exception {
        createOrderAndReturnId();

        addItemToCart();
        OrderRequest orderRequest = new OrderRequest(testAddress.getId());
        String secondResponse = mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long secondOrderId = objectMapper.readTree(secondResponse).get("id").asLong();

        mockMvc.perform(get("/api/v1/orders")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(secondOrderId.intValue())));
    }

    @Test
    @DisplayName("GET /api/v1/orders/{orderId} - Should return order details")
    void getOrderById_success() throws Exception {
        Long orderId = createOrderAndReturnId();

        mockMvc.perform(get("/api/v1/orders/" + orderId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(orderId.intValue())))
                .andExpect(jsonPath("$.status", is("PLACED")))
                .andExpect(jsonPath("$.items", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/v1/orders/{orderId} - Other user access returns 404")
    void getOrderById_otherUser_returns404() throws Exception {
        Long orderId = createOrderAndReturnId();

        mockMvc.perform(get("/api/v1/orders/" + orderId)
                        .header("Authorization", "Bearer " + otherJwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /api/v1/orders/{orderId}/cancel - Should cancel PLACED order")
    void cancelOrder_success() throws Exception {
        Long orderId = createOrderAndReturnId();

        mockMvc.perform(patch("/api/v1/orders/" + orderId + "/cancel")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));

        Order order = orderRepository.findById(orderId).orElseThrow();
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    @DisplayName("PATCH /api/v1/orders/{orderId}/cancel - Should fail for non-cancellable status")
    void cancelOrder_invalidStatus_returns400() throws Exception {
        Long orderId = createOrderAndReturnId();

        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setStatus(OrderStatus.PREPARING);
        orderRepository.save(order);

        mockMvc.perform(patch("/api/v1/orders/" + orderId + "/cancel")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.containsString("cannot be cancelled")));
    }

    @Test
    @DisplayName("POST /api/v1/orders - Should clear cart items after successful order")
    void createOrder_clearsCart() throws Exception {
        addItemToCart();
        OrderRequest orderRequest = new OrderRequest(testAddress.getId());

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/cart")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.totalAmount", is(0.0)));

        assertTrue(cartRepository.findByUserId(testUser.getId()).isPresent());
    }

}
