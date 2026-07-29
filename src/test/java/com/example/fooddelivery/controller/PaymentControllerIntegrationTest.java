package com.example.fooddelivery.controller;

import com.example.fooddelivery.dto.request.CartItemRequest;
import com.example.fooddelivery.dto.request.OrderRequest;
import com.example.fooddelivery.dto.request.PaymentRequest;
import com.example.fooddelivery.entity.Address;
import com.example.fooddelivery.entity.Menu;
import com.example.fooddelivery.entity.Order;
import com.example.fooddelivery.entity.Restaurant;
import com.example.fooddelivery.entity.User;
import com.example.fooddelivery.enums.OrderStatus;
import com.example.fooddelivery.enums.PaymentMethod;
import com.example.fooddelivery.enums.PaymentStatus;
import com.example.fooddelivery.enums.Role;
import com.example.fooddelivery.repository.AddressRepository;
import com.example.fooddelivery.repository.CartItemRepository;
import com.example.fooddelivery.repository.CartRepository;
import com.example.fooddelivery.repository.MenuRepository;
import com.example.fooddelivery.repository.OrderItemRepository;
import com.example.fooddelivery.repository.OrderRepository;
import com.example.fooddelivery.repository.PaymentRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerIntegrationTest {

    private static final String TEST_EMAIL = "payment-test@example.com";
    private static final String OTHER_EMAIL = "payment-other@example.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentRepository paymentRepository;

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
    private Address testAddress;
    private Menu availableMenu;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        addressRepository.deleteAll();
        menuRepository.deleteAll();
        restaurantRepository.deleteAll();

        testUser = userRepository.findByEmail(TEST_EMAIL).orElseGet(() -> userRepository.save(User.builder()
                .name("Payment Test User")
                .email(TEST_EMAIL)
                .password(passwordEncoder.encode("password123"))
                .role(Role.CUSTOMER)
                .build()));

        userRepository.findByEmail(OTHER_EMAIL).orElseGet(() -> userRepository.save(User.builder()
                .name("Payment Other User")
                .email(OTHER_EMAIL)
                .password(passwordEncoder.encode("password123"))
                .role(Role.CUSTOMER)
                .build()));

        jwtToken = jwtTokenProvider.generateToken(TEST_EMAIL, "CUSTOMER");
        otherJwtToken = jwtTokenProvider.generateToken(OTHER_EMAIL, "CUSTOMER");

        Restaurant activeRestaurant = restaurantRepository.save(Restaurant.builder()
                .name("Payment Test Restaurant")
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
                .city("New York")
                .state("NY")
                .postalCode("10001")
                .country("USA")
                .isDefault(true)
                .build());
    }

    private Long createOrder() throws Exception {
        CartItemRequest cartRequest = new CartItemRequest(availableMenu.getId(), 2);
        mockMvc.perform(post("/api/v1/cart/items")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartRequest)))
                .andExpect(status().isOk());

        OrderRequest orderRequest = new OrderRequest(testAddress.getId());
        String response = mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private PaymentRequest successPaymentRequest() {
        return new PaymentRequest(PaymentMethod.UPI, true);
    }

    @Test
    @DisplayName("POST /api/payments/order/{orderId} - Successful payment confirms order")
    void processPayment_success() throws Exception {
        Long orderId = createOrder();

        mockMvc.perform(post("/api/payments/order/" + orderId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(successPaymentRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentStatus", is("SUCCESS")))
                .andExpect(jsonPath("$.paymentMethod", is("UPI")))
                .andExpect(jsonPath("$.amount", is(20.0)))
                .andExpect(jsonPath("$.transactionId").exists())
                .andExpect(jsonPath("$.orderId", is(orderId.intValue())));

        Order order = orderRepository.findById(orderId).orElseThrow();
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
    }

    @Test
    @DisplayName("POST /api/payments/order/{orderId} - Failed payment keeps order PLACED")
    void processPayment_failure() throws Exception {
        Long orderId = createOrder();
        PaymentRequest request = new PaymentRequest(PaymentMethod.CARD, false);

        mockMvc.perform(post("/api/payments/order/" + orderId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentStatus", is("FAILED")));

        Order order = orderRepository.findById(orderId).orElseThrow();
        assertEquals(OrderStatus.PLACED, order.getStatus());
    }

    @Test
    @DisplayName("POST /api/payments/order/{orderId} - Already paid order returns 409")
    void processPayment_alreadyPaid_returns409() throws Exception {
        Long orderId = createOrder();

        mockMvc.perform(post("/api/payments/order/" + orderId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(successPaymentRequest())))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/payments/order/" + orderId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(successPaymentRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", is("Order is already paid")));
    }

    @Test
    @DisplayName("POST /api/payments/order/{orderId} - Other user's order returns 404")
    void processPayment_unauthorized_returns404() throws Exception {
        Long orderId = createOrder();

        mockMvc.perform(post("/api/payments/order/" + orderId)
                        .header("Authorization", "Bearer " + otherJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(successPaymentRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/payments/history - Returns user payments newest first")
    void getPaymentHistory_success() throws Exception {
        Long orderId1 = createOrder();
        mockMvc.perform(post("/api/payments/order/" + orderId1)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(successPaymentRequest())))
                .andExpect(status().isCreated());

        Long orderId2 = createOrder();
        String secondPaymentResponse = mockMvc.perform(post("/api/payments/order/" + orderId2)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(successPaymentRequest())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long secondPaymentId = objectMapper.readTree(secondPaymentResponse).get("id").asLong();

        mockMvc.perform(get("/api/payments/history")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(secondPaymentId.intValue())))
                .andExpect(jsonPath("$[0].paymentStatus", is(PaymentStatus.SUCCESS.name())));
    }

}
