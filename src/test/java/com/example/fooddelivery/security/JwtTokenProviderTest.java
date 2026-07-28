package com.example.fooddelivery.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    // Base64-encoded 32-byte secret for HS256
    private static final String TEST_SECRET = "dGhpcyBpcyBhIGRldmVsb3BtZW50IG9ubHkgc2VjcmV0IGtleQ==";
    private static final long TEST_EXPIRATION = 86400000L; // 24 hours

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(TEST_SECRET, TEST_EXPIRATION);
    }

    @Test
    @DisplayName("Should generate a non-null, non-empty token")
    void generateToken_shouldReturnNonEmptyToken() {
        String token = jwtTokenProvider.generateToken("john@example.com", "CUSTOMER");

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("Should extract correct email from token")
    void getEmailFromToken_shouldReturnCorrectEmail() {
        String email = "john@example.com";
        String token = jwtTokenProvider.generateToken(email, "CUSTOMER");

        String extractedEmail = jwtTokenProvider.getEmailFromToken(token);

        assertEquals(email, extractedEmail);
    }

    @Test
    @DisplayName("Should extract correct role from token")
    void getRoleFromToken_shouldReturnCorrectRole() {
        String role = "ADMIN";
        String token = jwtTokenProvider.generateToken("john@example.com", role);

        String extractedRole = jwtTokenProvider.getRoleFromToken(token);

        assertEquals(role, extractedRole);
    }

    @Test
    @DisplayName("Should return true for a valid token")
    void validateToken_withValidToken_shouldReturnTrue() {
        String token = jwtTokenProvider.generateToken("john@example.com", "CUSTOMER");

        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    @DisplayName("Should return false for a tampered token")
    void validateToken_withTamperedToken_shouldReturnFalse() {
        String token = jwtTokenProvider.generateToken("john@example.com", "CUSTOMER");
        String tamperedToken = token + "tampered";

        assertFalse(jwtTokenProvider.validateToken(tamperedToken));
    }

    @Test
    @DisplayName("Should return false for an expired token")
    void validateToken_withExpiredToken_shouldReturnFalse() {
        // Create a provider with 0ms expiration — token is immediately expired
        JwtTokenProvider expiredProvider = new JwtTokenProvider(TEST_SECRET, 0L);
        String token = expiredProvider.generateToken("john@example.com", "CUSTOMER");

        assertFalse(expiredProvider.validateToken(token));
    }

}
