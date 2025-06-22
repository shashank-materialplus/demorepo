package com.springbootmicroservices.userservice.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserNotFoundExceptionTest {

    @Test
    void testParameterizedConstructor() {
        // Arrange
        String customMessage = "User with ID 123 not found.";

        // Act
        UserNotFoundException exception = new UserNotFoundException(customMessage);

        // Assert
        // This test now correctly checks the only available constructor.
        assertEquals(customMessage, exception.getMessage());
    }
}