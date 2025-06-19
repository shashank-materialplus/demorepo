package com.springbootmicroservices.orderservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import java.io.Serial;

@ResponseStatus(HttpStatus.BAD_REQUEST) // Or 409 Conflict if it's about a state transition
public class InvalidOrderStatusException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 4L; // Example UID

    public InvalidOrderStatusException(String message) {
        super(message);
    }

    public InvalidOrderStatusException(String message, Throwable cause) {
        super(message, cause);
    }
}