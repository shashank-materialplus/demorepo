package com.springbootmicroservices.orderservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import java.io.Serial;

@ResponseStatus(HttpStatus.NOT_FOUND) // This helps Spring map it to a 404 if not caught by GlobalExceptionHandler
public class OrderNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L; // Example UID, generate a proper one

    public OrderNotFoundException(String message) {
        super(message);
    }

    public OrderNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}