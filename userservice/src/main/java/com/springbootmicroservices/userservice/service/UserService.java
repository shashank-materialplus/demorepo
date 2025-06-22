package com.springbootmicroservices.userservice.service;

import com.springbootmicroservices.userservice.model.user.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    List<UserResponse> getAllUsers();
    void deleteUserById(String userId);
    UserResponse getUserById(String userId);
}