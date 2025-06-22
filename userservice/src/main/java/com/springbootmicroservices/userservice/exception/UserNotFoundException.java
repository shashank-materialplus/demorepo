package com.springbootmicroservices.userservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

/**
 * Exception named {@link UserNotFoundException} thrown when a requested user cannot be found.
 */
// --- THIS IS THE CRITICAL ADDITION ---
// This annotation tells Spring to automatically return a 404 HTTP status
// whenever this exception is thrown from a controller or service.
@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -3952215105519401565L;

    private static final String DEFAULT_MESSAGE = """
            User not found!
            """;

    /**
     * Constructs a {@code UserNotFoundException} with the default message.
     */
    public UserNotFoundException() {
        super(DEFAULT_MESSAGE);
    }

    /**
     * Constructs a {@code UserNotFoundException} with a custom message.
     *
     * @param message the detail message
     */
    public UserNotFoundException(final String message) {
        super(message); // Using super(message) is cleaner than appending to the default.
    }

}