package com.example.fooddelivery.mapper;

import com.example.fooddelivery.dto.response.UserResponse;
import com.example.fooddelivery.entity.User;

public class UserMapper {

    private UserMapper() {
    }

    public static UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

}
