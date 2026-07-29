package com.example.fooddelivery.controller;

import com.example.fooddelivery.dto.request.LoginRequest;
import com.example.fooddelivery.dto.request.RegisterRequest;
import com.example.fooddelivery.repository.AddressRepository;
import com.example.fooddelivery.enums.Role;
import com.example.fooddelivery.repository.CartItemRepository;
import com.example.fooddelivery.repository.CartRepository;
import com.example.fooddelivery.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private AddressRepository addressRepository;

    @BeforeEach
    void setUp() {
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        addressRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Should register successfully and return 201")
    void register_success() throws Exception {
        RegisterRequest request = new RegisterRequest("John Doe", "john@example.com", "password123", Role.CUSTOMER);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", is(notNullValue())))
                .andExpect(jsonPath("$.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.user.name", is("John Doe")))
                .andExpect(jsonPath("$.user.email", is("john@example.com")))
                .andExpect(jsonPath("$.user.role", is("CUSTOMER")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Should return 409 for duplicate email")
    void register_duplicateEmail() throws Exception {
        RegisterRequest request = new RegisterRequest("John Doe", "john@example.com", "password123", Role.CUSTOMER);

        // First registration
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Duplicate registration
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.message", is("User already exists with email: john@example.com")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Should login successfully and return 200")
    void login_success() throws Exception {
        // Register first
        RegisterRequest registerRequest = new RegisterRequest("John Doe", "john@example.com", "password123", Role.CUSTOMER);
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Login
        LoginRequest loginRequest = new LoginRequest("john@example.com", "password123");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is(notNullValue())))
                .andExpect(jsonPath("$.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.user.email", is("john@example.com")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Should return 401 for wrong password")
    void login_wrongPassword() throws Exception {
        // Register first
        RegisterRequest registerRequest = new RegisterRequest("John Doe", "john@example.com", "password123", Role.CUSTOMER);
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Login with wrong password
        LoginRequest loginRequest = new LoginRequest("john@example.com", "wrongpassword");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.message", is("Invalid email or password")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Should return 400 for validation failures")
    void register_validationFailure() throws Exception {
        // Empty name, invalid email, short password, missing role
        String invalidJson = "{\"name\":\"\",\"email\":\"not-an-email\",\"password\":\"short\"}";

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is(notNullValue())));
    }

}
