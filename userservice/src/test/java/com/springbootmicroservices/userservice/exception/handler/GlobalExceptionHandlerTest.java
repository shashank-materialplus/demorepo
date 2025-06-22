package com.springbootmicroservices.userservice.exception.handler;

import com.springbootmicroservices.userservice.exception.*;
import com.springbootmicroservices.userservice.model.common.CustomError;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.authorization.AuthorizationDecision;

import static org.assertj.core.api.Assertions.assertThat;

// Use MockitoExtension to enable @InjectMocks
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    void givenUserNotFoundException_whenHandled_thenReturnsNotFound() {
        // Given
        UserNotFoundException ex = new UserNotFoundException("User with id 123 not found");

        // When
        ResponseEntity<Object> response = globalExceptionHandler.handleUserNotFoundException(ex);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        CustomError body = (CustomError) response.getBody();
        assertThat(body.getMessage()).isEqualTo("User with id 123 not found");
    }

    @Test
    void givenPasswordNotValidException_whenHandled_thenReturnsBadRequest() {
        // Given
        PasswordNotValidException ex = new PasswordNotValidException("Password does not meet criteria.");

        // When
        ResponseEntity<CustomError> response = globalExceptionHandler.handlePasswordNotValidException(ex);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getHeader()).isEqualTo(CustomError.Header.VALIDATION_ERROR.getName());
        assertThat(response.getBody().getMessage()).contains("Password does not meet criteria.");
    }

    // You can add tests for your other custom exceptions following this pattern.
    // ... e.g., for UserAlreadyExistException, TokenAlreadyInvalidatedException, etc.
}