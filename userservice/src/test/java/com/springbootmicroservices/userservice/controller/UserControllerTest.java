package com.springbootmicroservices.userservice.controller;

import com.springbootmicroservices.userservice.base.AbstractRestControllerTest;
import com.springbootmicroservices.userservice.model.user.Token;
import com.springbootmicroservices.userservice.model.user.dto.request.LoginRequest;
import com.springbootmicroservices.userservice.model.user.dto.request.RegisterRequest;
import com.springbootmicroservices.userservice.model.user.dto.response.UserResponse;
import com.springbootmicroservices.userservice.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest extends AbstractRestControllerTest {

    @MockBean
    private RegisterService registerService;
    @MockBean
    private UserLoginService userLoginService;
    @MockBean
    private RefreshTokenService refreshTokenService;
    @MockBean
    private LogoutService logoutService;
    @MockBean
    private TokenService tokenService;
    @MockBean
    private UserService userService; // Mock the new service

    @Test
    void registerUser_PublicEndpoint_ShouldReturnSuccess() throws Exception {
        // Given: A COMPLETE and VALID request object
        RegisterRequest request = RegisterRequest.builder()
                .email("new@user.com")
                .password("password1234")
                .firstName("Test")
                .lastName("User")
                .phoneNumber("1234567890")
                .role("USER")
                .build();

        // We are testing a void method, so no "when" stub is needed.

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true));

        verify(registerService).registerUser(any(RegisterRequest.class));
    }

    @Test
    void loginUser_PublicEndpoint_ShouldReturnToken() throws Exception {
        // Given
        LoginRequest request = LoginRequest.builder().email("test@user.com").password("password").build();
        Token mockToken = Token.builder().accessToken("abc").refreshToken("xyz").build();
        when(userLoginService.login(any(LoginRequest.class))).thenReturn(mockToken);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.accessToken").value("abc"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN") // Simulate an admin user
    void getAllUsers_AsAdmin_ShouldReturnUserList() throws Exception {
        // Given
        UserResponse user = UserResponse.builder().email("test@user.com").build();
        List<UserResponse> userList = Collections.singletonList(user);
        when(userService.getAllUsers()).thenReturn(userList);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.response[0].email").value("test@user.com"));

        verify(userService).getAllUsers();
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER") // Simulate a non-admin user
    void getAllUsers_AsUser_ShouldReturnForbidden() throws Exception {
        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void deleteUser_AsAdmin_ShouldReturnSuccess() throws Exception {
        // Given
        String userIdToDelete = "user-id-123";
        doNothing().when(userService).deleteUserById(userIdToDelete);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/users/{userId}", userIdToDelete))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true));

        verify(userService).deleteUserById(userIdToDelete);
    }
}