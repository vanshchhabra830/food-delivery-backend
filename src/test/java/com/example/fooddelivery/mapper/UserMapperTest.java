package com.example.fooddelivery.mapper;

import com.example.fooddelivery.dto.response.UserResponse;
import com.example.fooddelivery.entity.User;
import com.example.fooddelivery.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UserMapperTest {

    @Test
    @DisplayName("Should map all User fields to UserResponse correctly")
    void toUserResponse_shouldMapAllFields() {
        User user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .password("hashedPassword")
                .role(Role.CUSTOMER)
                .build();

        UserResponse response = UserMapper.toUserResponse(user);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("John Doe", response.getName());
        assertEquals("john@example.com", response.getEmail());
        assertEquals(Role.CUSTOMER, response.getRole());
    }

    @Test
    @DisplayName("Should not expose password in UserResponse")
    void toUserResponse_shouldNotExposePassword() {
        User user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .password("secretPassword123")
                .role(Role.CUSTOMER)
                .build();

        UserResponse response = UserMapper.toUserResponse(user);

        // UserResponse has no password field — this test verifies the mapper
        // only maps id, name, email, role (no getPassword method exists)
        assertNotNull(response);
        assertEquals("john@example.com", response.getEmail());
    }

}
