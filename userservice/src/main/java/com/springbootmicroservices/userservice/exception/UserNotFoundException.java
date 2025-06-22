package com.springbootmicroservices.userservice.exception;

import java.io.Serial;

public class UserNotFoundException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -3952215105519401565L;

    /**
     * Constructs a UserNotFoundException with a specific detail message.
     * @param message the full detail message.
     */
    public UserNotFoundException(final String message) {
        super(message); // Only use the provided message
    }
}