package com.springbootmicroservices.orderservice.model.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor; // For Header enum
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CustomError {

    @Builder.Default
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS") // Consistent timestamp format
    private LocalDateTime time = LocalDateTime.now();

    private HttpStatus httpStatus; // e.g., BAD_REQUEST, NOT_FOUND

    private String header; // Custom header string from the Header enum

    @JsonInclude(JsonInclude.Include.NON_NULL) // Don't include if null
    private String message; // Main error message

    @Builder.Default
    private final Boolean isSuccess = false; // Always false for errors

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<CustomSubError> subErrors; // For validation errors or detailed issues

    /**
     * Represents a sub-error, typically used for field-specific validation errors.
     */
    @Getter
    @Builder
    public static class CustomSubError {
        private String field;       // The field that caused the error
        private String message;     // The specific error message for the field
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private Object value;       // The value that was rejected
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String type;        // The type of the object/field
    }

    /**
     * Enumerates common error headers for categorizing errors.
     * Ensure these match what your GlobalExceptionHandler uses.
     */
    @Getter
    @RequiredArgsConstructor
    public enum Header {
        API_ERROR("API_ERROR"), // General API error
        AUTH_ERROR("AUTH_ERROR"), // Authentication specific error
        VALIDATION_ERROR("VALIDATION_ERROR"),
        NOT_FOUND("NOT_FOUND"), // Resource not found
        ALREADY_EXIST("ALREADY_EXIST"), // Resource conflict
        PROCESS_ERROR("PROCESS_ERROR"), // Business logic/processing error
        DATABASE_ERROR("DATABASE_ERROR"),
        INSUFFICIENT_STOCK("INSUFFICIENT_STOCK"); // Specific for order service

        private final String name;
    }
}