package com.springbootmicroservices.orderservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import java.io.Serial;

@ResponseStatus(HttpStatus.BAD_REQUEST) // Or 500/502 if it's an issue with the payment gateway itself
public class PaymentProcessingException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 3L; // Example UID

    public PaymentProcessingException(String message) {
        super(message);
    }

    public PaymentProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}