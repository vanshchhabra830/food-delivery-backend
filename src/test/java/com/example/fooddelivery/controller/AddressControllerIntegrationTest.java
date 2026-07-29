package com.example.fooddelivery.controller;

import com.example.fooddelivery.dto.request.AddressRequest;
import com.example.fooddelivery.entity.Address;
import com.example.fooddelivery.entity.User;
import com.example.fooddelivery.enums.Role;
import com.example.fooddelivery.repository.AddressRepository;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AddressControllerIntegrationTest {

    private static final String TEST_EMAIL = "address-test@example.com";
    private static final String OTHER_EMAIL = "address-other@example.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AddressRepository addressRepository;

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

    @BeforeEach
    void setUp() {
        addressRepository.deleteAll();

        if (!userRepository.existsByEmail(TEST_EMAIL)) {
            testUser = User.builder()
                    .name("Address Test User")
                    .email(TEST_EMAIL)
                    .password(passwordEncoder.encode("password123"))
                    .role(Role.CUSTOMER)
                    .build();
            testUser = userRepository.save(testUser);
        } else {
            testUser = userRepository.findByEmail(TEST_EMAIL).get();
        }

        if (!userRepository.existsByEmail(OTHER_EMAIL)) {
            otherUser = User.builder()
                    .name("Address Other User")
                    .email(OTHER_EMAIL)
                    .password(passwordEncoder.encode("password123"))
                    .role(Role.CUSTOMER)
                    .build();
            otherUser = userRepository.save(otherUser);
        } else {
            otherUser = userRepository.findByEmail(OTHER_EMAIL).get();
        }

        jwtToken = jwtTokenProvider.generateToken(TEST_EMAIL, "CUSTOMER");
        otherJwtToken = jwtTokenProvider.generateToken(OTHER_EMAIL, "CUSTOMER");
    }

    private AddressRequest createValidRequest() {
        return new AddressRequest(
                "John Doe",
                "1234567890",
                "123 Main St",
                "Apt 4B",
                "Near Park",
                "New York",
                "NY",
                "10001",
                "USA",
                false
        );
    }

    @Test
    @DisplayName("POST /api/v1/addresses - Should add first address and make it default")
    void addAddress_firstAddress_becomesDefault() throws Exception {
        AddressRequest request = createValidRequest();
        
        mockMvc.perform(post("/api/v1/addresses")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fullName", is("John Doe")))
                .andExpect(jsonPath("$.isDefault", is(true))); // Auto default
    }

    @Test
    @DisplayName("POST /api/v1/addresses - Should add second address normally")
    void addAddress_secondAddress_notDefault() throws Exception {
        // Add first address
        Address firstAddress = Address.builder()
                .user(testUser).fullName("First").phoneNumber("1234567890")
                .addressLine1("Line1").city("City").state("State")
                .postalCode("12345").country("Country").isDefault(true)
                .build();
        addressRepository.save(firstAddress);

        AddressRequest request = createValidRequest();
        
        mockMvc.perform(post("/api/v1/addresses")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isDefault", is(false)));
    }

    @Test
    @DisplayName("POST /api/v1/addresses - Add with isDefault=true removes previous default")
    void addAddress_withIsDefaultTrue_removesPreviousDefault() throws Exception {
        Address firstAddress = Address.builder()
                .user(testUser).fullName("First").phoneNumber("1234567890")
                .addressLine1("Line1").city("City").state("State")
                .postalCode("12345").country("Country").isDefault(true)
                .build();
        addressRepository.save(firstAddress);

        AddressRequest request = createValidRequest();
        request.setIsDefault(true);
        
        mockMvc.perform(post("/api/v1/addresses")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isDefault", is(true)));

        Address previousDefault = addressRepository.findById(firstAddress.getId()).get();
        assertFalse(previousDefault.getIsDefault());
    }

    @Test
    @DisplayName("GET /api/v1/addresses - Return user's addresses")
    void getAllAddresses_success() throws Exception {
        Address firstAddress = Address.builder()
                .user(testUser).fullName("First").phoneNumber("1234567890")
                .addressLine1("Line1").city("City").state("State")
                .postalCode("12345").country("Country").isDefault(true)
                .build();
        addressRepository.save(firstAddress);

        mockMvc.perform(get("/api/v1/addresses")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].fullName", is("First")));
    }

    @Test
    @DisplayName("GET /api/v1/addresses/{addressId} - Success")
    void getAddressById_success() throws Exception {
        Address address = Address.builder()
                .user(testUser).fullName("Target").phoneNumber("1234567890")
                .addressLine1("Line1").city("City").state("State")
                .postalCode("12345").country("Country").isDefault(true)
                .build();
        address = addressRepository.save(address);

        mockMvc.perform(get("/api/v1/addresses/" + address.getId())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName", is("Target")));
    }

    @Test
    @DisplayName("PUT /api/v1/addresses/{addressId} - Update address")
    void updateAddress_success() throws Exception {
        Address address = Address.builder()
                .user(testUser).fullName("Old").phoneNumber("1234567890")
                .addressLine1("Line1").city("City").state("State")
                .postalCode("12345").country("Country").isDefault(false)
                .build();
        address = addressRepository.save(address);

        AddressRequest updateRequest = createValidRequest();
        updateRequest.setFullName("New Name");

        mockMvc.perform(put("/api/v1/addresses/" + address.getId())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName", is("New Name")));
    }

    @Test
    @DisplayName("PATCH /api/v1/addresses/{addressId}/default - Set as default")
    void setDefaultAddress_success() throws Exception {
        Address firstAddress = Address.builder()
                .user(testUser).fullName("First").phoneNumber("1234567890")
                .addressLine1("Line1").city("City").state("State")
                .postalCode("12345").country("Country").isDefault(true)
                .build();
        addressRepository.save(firstAddress);

        Address secondAddress = Address.builder()
                .user(testUser).fullName("Second").phoneNumber("1234567890")
                .addressLine1("Line1").city("City").state("State")
                .postalCode("12345").country("Country").isDefault(false)
                .build();
        secondAddress = addressRepository.save(secondAddress);

        mockMvc.perform(patch("/api/v1/addresses/" + secondAddress.getId() + "/default")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isDefault", is(true)));

        Address updatedFirst = addressRepository.findById(firstAddress.getId()).get();
        assertFalse(updatedFirst.getIsDefault());
    }

    @Test
    @DisplayName("DELETE /api/v1/addresses/{addressId} - Delete normal address")
    void deleteAddress_normal_success() throws Exception {
        Address firstAddress = Address.builder()
                .user(testUser).fullName("First").phoneNumber("1234567890")
                .addressLine1("Line1").city("City").state("State")
                .postalCode("12345").country("Country").isDefault(true)
                .build();
        addressRepository.save(firstAddress);

        Address secondAddress = Address.builder()
                .user(testUser).fullName("Second").phoneNumber("1234567890")
                .addressLine1("Line1").city("City").state("State")
                .postalCode("12345").country("Country").isDefault(false)
                .build();
        secondAddress = addressRepository.save(secondAddress);

        mockMvc.perform(delete("/api/v1/addresses/" + secondAddress.getId())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());

        assertFalse(addressRepository.findById(secondAddress.getId()).isPresent());
        assertTrue(addressRepository.findById(firstAddress.getId()).get().getIsDefault());
    }

    @Test
    @DisplayName("DELETE /api/v1/addresses/{addressId} - Delete default assigns new default")
    void deleteAddress_default_assignsNewDefault() throws Exception {
        Address firstAddress = Address.builder()
                .user(testUser).fullName("First").phoneNumber("1234567890")
                .addressLine1("Line1").city("City").state("State")
                .postalCode("12345").country("Country").isDefault(false)
                .build();
        firstAddress = addressRepository.save(firstAddress);

        Address secondAddress = Address.builder()
                .user(testUser).fullName("Second").phoneNumber("1234567890")
                .addressLine1("Line1").city("City").state("State")
                .postalCode("12345").country("Country").isDefault(true)
                .build();
        secondAddress = addressRepository.save(secondAddress);

        mockMvc.perform(delete("/api/v1/addresses/" + secondAddress.getId())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());

        Address newDefault = addressRepository.findById(firstAddress.getId()).get();
        assertTrue(newDefault.getIsDefault());
    }

    @Test
    @DisplayName("DELETE /api/v1/addresses/{addressId} - Delete last address")
    void deleteAddress_lastAddress_success() throws Exception {
        Address address = Address.builder()
                .user(testUser).fullName("Only").phoneNumber("1234567890")
                .addressLine1("Line1").city("City").state("State")
                .postalCode("12345").country("Country").isDefault(true)
                .build();
        address = addressRepository.save(address);

        mockMvc.perform(delete("/api/v1/addresses/" + address.getId())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());
                
        assertFalse(addressRepository.findById(address.getId()).isPresent());
    }

    @Test
    @DisplayName("GET /api/v1/addresses/{addressId} - Access other user's address returns 404")
    void getAddress_otherUser_returns404() throws Exception {
        Address address = Address.builder()
                .user(testUser).fullName("Target").phoneNumber("1234567890")
                .addressLine1("Line1").city("City").state("State")
                .postalCode("12345").country("Country").isDefault(true)
                .build();
        address = addressRepository.save(address);

        mockMvc.perform(get("/api/v1/addresses/" + address.getId())
                .header("Authorization", "Bearer " + otherJwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/addresses - Validation error for invalid phone number")
    void addAddress_invalidPhone_returns400() throws Exception {
        AddressRequest request = createValidRequest();
        request.setPhoneNumber("123"); // Too short
        
        mockMvc.perform(post("/api/v1/addresses")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.containsString("Phone number must be exactly 10 digits")));
    }

    @Test
    @DisplayName("PUT /api/v1/addresses/{addressId} - Manually unsetting default via PUT returns 400")
    void updateAddress_unsetDefault_returns400() throws Exception {
        Address address = Address.builder()
                .user(testUser).fullName("Target").phoneNumber("1234567890")
                .addressLine1("Line1").city("City").state("State")
                .postalCode("12345").country("Country").isDefault(true)
                .build();
        address = addressRepository.save(address);

        AddressRequest request = createValidRequest();
        request.setIsDefault(false); // Trying to unset manually
        
        mockMvc.perform(put("/api/v1/addresses/" + address.getId())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Cannot manually unset default address. Set another address as default instead.")));
    }
}
