// src/main/java/com/springbootmicroservices/productservice/exception/InsufficientStockException.java
package com.springbootmicroservices.productservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import java.io.Serial;

@ResponseStatus(HttpStatus.CONFLICT) // Or BAD_REQUEST, depending on how you view it
public class InsufficientStockException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L; // Add a unique serialVersionUID

    public InsufficientStockException(String message) {
        super(message);
    }
}