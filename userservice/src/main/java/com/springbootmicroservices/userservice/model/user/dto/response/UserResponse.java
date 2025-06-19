package com.springbootmicroservices.userservice.model.user.dto.response;

import com.springbootmicroservices.userservice.model.user.enums.UserStatus;
import com.springbootmicroservices.userservice.model.user.enums.UserType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserResponse {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private UserStatus userStatus;
    private UserType userType;
}