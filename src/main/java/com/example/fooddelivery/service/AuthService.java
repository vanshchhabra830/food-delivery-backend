package com.example.fooddelivery.service;

import com.example.fooddelivery.dto.request.LoginRequest;
import com.example.fooddelivery.dto.request.RegisterRequest;
import com.example.fooddelivery.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

}
