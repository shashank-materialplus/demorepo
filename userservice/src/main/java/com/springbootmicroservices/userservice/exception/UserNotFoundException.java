package com.springbootmicroservices.userservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import java.io.Serial;

/**
 * Exception thrown when a requested user cannot be found.
 * The @ResponseStatus annotation provides a default HTTP status mapping for this exception.
 */
@ResponseStatus(HttpStatus.NOT_FOUND) // Keep this valuable annotation
public class UserNotFoundException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -3952215105519401565L;

    /**
     * Constructs a UserNotFoundException with a specific detail message.
     * @param message The full detail message explaining the error.
     */
    public UserNotFoundException(final String message) {
        // Keep the clean constructor that only takes the message.
        super(message);
    }
}