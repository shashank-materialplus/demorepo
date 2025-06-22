package com.springbootmicroservices.productservice.exception;

import java.io.Serial;

/**
 * Exception class thrown when a requested product cannot be found.
 */
public class ProductNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 5854010258697200749L;

    /**
     * Constructs a new ProductNotFoundException with a specific detail message.
     *
     * @param message the full detail message.
     */
    public ProductNotFoundException(final String message) {
        // We only call super with the provided message.
        super(message);
    }
}