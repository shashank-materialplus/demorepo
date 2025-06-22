package com.springbootmicroservices.productservice.exception;

import java.io.Serial;

/**
 * Exception class thrown when attempting to create a product that already exists.
 */
public class ProductAlreadyExistException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 53457089789182737L;

    /**
     * Constructs a new ProductAlreadyExistException with a specific detail message.
     * The caller is responsible for providing the full, user-friendly message.
     *
     * @param message the full detail message.
     */
    public ProductAlreadyExistException(final String message) {
        // We only call super with the provided message. No default is prepended.
        super(message);
    }
}