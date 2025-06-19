package com.springbootmicroservices.orderservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import java.io.Serial;

@ResponseStatus(HttpStatus.CONFLICT) // 409 Conflict is appropriate for business rule violation like insufficient stock
public class InsufficientStockException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 2L; // Example UID

    public InsufficientStockException(String message) {
        super(message);
    }

    public InsufficientStockException(String message, Throwable cause) {
        super(message, cause);
    }
}