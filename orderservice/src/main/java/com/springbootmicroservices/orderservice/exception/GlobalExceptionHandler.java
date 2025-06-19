package com.springbootmicroservices.orderservice.exception;

import com.springbootmicroservices.orderservice.model.common.CustomError;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException; // For handling JWT validation errors from TokenService

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // --- Standard Spring Validation Handlers ---

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<CustomError> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        log.warn("Validation Error (Method Argument): {}", ex.getMessage());
        List<CustomError.CustomSubError> subErrors = ex.getBindingResult().getAllErrors().stream()
                .map(error -> CustomError.CustomSubError.builder()
                        .field(error instanceof FieldError ? ((FieldError) error).getField() : error.getObjectName())
                        .message(error.getDefaultMessage())
                        .value(error instanceof FieldError ? ((FieldError) error).getRejectedValue() : null)
                        .type(error.getObjectName())
                        .build())
                .collect(Collectors.toList());

        CustomError customError = CustomError.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .header(CustomError.Header.VALIDATION_ERROR.getName())
                .message("Validation failed. Please check your input.")
                .subErrors(subErrors)
                .build();
        return new ResponseEntity<>(customError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<CustomError> handleConstraintViolation(ConstraintViolationException ex) {
        log.warn("Validation Error (Constraint Violation): {}", ex.getMessage());
        List<CustomError.CustomSubError> subErrors = ex.getConstraintViolations().stream()
                .map(cv -> CustomError.CustomSubError.builder()
                        .message(cv.getMessage())
                        .field(StringUtils.substringAfterLast(cv.getPropertyPath().toString(), "."))
                        .value(cv.getInvalidValue() != null ? cv.getInvalidValue().toString() : null)
                        .type(cv.getRootBeanClass().getSimpleName())
                        .build())
                .collect(Collectors.toList());

        CustomError customError = CustomError.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .header(CustomError.Header.VALIDATION_ERROR.getName())
                .message("Constraint violation. Please check your input.")
                .subErrors(subErrors)
                .build();
        return new ResponseEntity<>(customError, HttpStatus.BAD_REQUEST);
    }

    // --- Order Service Specific Exception Handlers ---

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<CustomError> handleOrderNotFoundException(OrderNotFoundException ex) {
        log.warn("Order Not Found: {}", ex.getMessage());
        CustomError error = CustomError.builder()
                .httpStatus(HttpStatus.NOT_FOUND)
                .header(CustomError.Header.NOT_FOUND.getName()) // Ensure this header exists in CustomError
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<CustomError> handleInsufficientStockException(InsufficientStockException ex) {
        log.warn("Insufficient Stock: {}", ex.getMessage());
        CustomError error = CustomError.builder()
                .httpStatus(HttpStatus.CONFLICT) // 409 Conflict for business rule violation
                .header(CustomError.Header.PROCESS_ERROR.getName()) // Or a more specific header
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(PaymentProcessingException.class)
    public ResponseEntity<CustomError> handlePaymentProcessingException(PaymentProcessingException ex) {
        log.error("Payment Processing Error: {}", ex.getMessage(), ex.getCause()); // Log cause if present
        CustomError error = CustomError.builder()
                .httpStatus(HttpStatus.BAD_REQUEST) // Or map to 5xx if it's a gateway/provider issue
                .header(CustomError.Header.PROCESS_ERROR.getName())
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidOrderStatusException.class)
    public ResponseEntity<CustomError> handleInvalidOrderStatusException(InvalidOrderStatusException ex) {
        log.warn("Invalid Order Status: {}", ex.getMessage());
        CustomError error = CustomError.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .header(CustomError.Header.VALIDATION_ERROR.getName()) // Or PROCESS_ERROR
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // --- Security Related Exception Handlers ---

    @ExceptionHandler(AccessDeniedException.class) // From Spring Security's @PreAuthorize
    public ResponseEntity<CustomError> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access Denied: {}", ex.getMessage());
        CustomError customError = CustomError.builder()
                .httpStatus(HttpStatus.FORBIDDEN)
                .header(CustomError.Header.AUTH_ERROR.getName())
                .message("Access Denied: You do not have permission to perform this action.")
                .build();
        return new ResponseEntity<>(customError, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ResponseStatusException.class) // Often thrown by TokenService for JWT validation issues
    public ResponseEntity<CustomError> handleResponseStatusException(ResponseStatusException ex) {
        log.warn("Response Status Exception: {} - {}", ex.getStatusCode(), ex.getReason());
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        CustomError customError = CustomError.builder()
                .httpStatus(status)
                .header(CustomError.Header.API_ERROR.getName()) // Or AUTH_ERROR if always auth related
                .message(ex.getReason())
                .build();
        return new ResponseEntity<>(customError, status);
    }

    // --- Generic Fallback Handler ---

    @ExceptionHandler(Exception.class) // Fallback for any other unhandled exceptions
    public ResponseEntity<CustomError> handleGenericException(Exception ex) {
        log.error("An unexpected error occurred: {}", ex.getMessage(), ex); // Log stack trace for unexpected
        CustomError customError = CustomError.builder()
                .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .header(CustomError.Header.API_ERROR.getName())
                .message("An unexpected internal error occurred. Please try again later.")
                .build();
        return new ResponseEntity<>(customError, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}