package com.springbootmicroservices.userservice.model.user.mapper;

import com.springbootmicroservices.userservice.model.user.dto.response.UserResponse;
import com.springbootmicroservices.userservice.model.user.entity.UserEntity;
import com.springbootmicroservices.userservice.model.common.mapper.BaseMapper;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class UserEntityToUserResponseMapper implements BaseMapper<UserEntity, UserResponse> {

    @Override
    public UserResponse map(UserEntity source) {
        return UserResponse.builder()
                .id(source.getId())
                .email(source.getEmail())
                .firstName(source.getFirstName())
                .lastName(source.getLastName())
                .phoneNumber(source.getPhoneNumber())
                .userStatus(source.getUserStatus())
                .userType(source.getUserType())
                .build();
    }

    @Override
    public List<UserResponse> map(java.util.Collection<UserEntity> sources) {
        return sources.stream().map(this::map).collect(Collectors.toList());
    }

    public static UserEntityToUserResponseMapper initialize() {
        return new UserEntityToUserResponseMapper();
    }
}