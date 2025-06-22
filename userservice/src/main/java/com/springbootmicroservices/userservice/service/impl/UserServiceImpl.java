package com.springbootmicroservices.userservice.service.impl;

import com.springbootmicroservices.userservice.exception.UserNotFoundException;
import com.springbootmicroservices.userservice.model.user.dto.response.UserResponse;
import com.springbootmicroservices.userservice.model.user.entity.UserEntity;
import com.springbootmicroservices.userservice.model.user.mapper.UserEntityToUserResponseMapper;
import com.springbootmicroservices.userservice.repository.UserRepository;
import com.springbootmicroservices.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserEntityToUserResponseMapper userMapper = UserEntityToUserResponseMapper.initialize();

    @Override
    public UserResponse getUserById(String userId) {

        // 1. Correctly declare the variable as UserEntity
        final UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        // 2. The mapper can now correctly map from UserEntity to UserResponse
        return userMapper.map(userEntity);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        List<UserEntity> userEntities = userRepository.findAll();
        return userMapper.map(userEntities);
    }

    @Override
    public void deleteUserById(String userId) {
        // First, verify the user exists before attempting to delete
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with id: " + userId);
        }
        userRepository.deleteById(userId);
    }
}