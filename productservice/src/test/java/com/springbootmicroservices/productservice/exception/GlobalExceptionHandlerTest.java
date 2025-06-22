package com.springbootmicroservices.productservice.exception;

import com.springbootmicroservices.productservice.model.common.CustomError;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.security.authorization.AuthorizationDeniedException;

import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class) // Enables Mockito annotations like @InjectMocks
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    void givenMethodArgumentNotValidException_whenHandled_thenRespondWithBadRequest() {
        BindingResult bindingResult = mock(BindingResult.class);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);
        FieldError fieldError = new FieldError("objectName", "fieldName", "error message");
        when(bindingResult.getAllErrors()).thenReturn(Collections.singletonList(fieldError));

        ResponseEntity<Object> responseEntity = globalExceptionHandler.handleMethodArgumentNotValid(ex);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        CustomError actualError = (CustomError) responseEntity.getBody();
        assertThat(actualError.getHeader()).isEqualTo(CustomError.Header.VALIDATION_ERROR.getName());
    }

    @Test
    void givenConstraintViolationException_whenHandled_thenRespondWithBadRequest() {
        // Given
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(violation.getPropertyPath()).thenReturn(path);
        when(path.toString()).thenReturn("some.field");
        when(violation.getMessage()).thenReturn("must not be empty");

        // THIS STUBBING IS NOT NEEDED and was causing the error. REMOVE IT.
        // when(violation.getInvalidValue()).thenReturn("");

        ConstraintViolationException ex = new ConstraintViolationException(Collections.singleton(violation));

        // When
        ResponseEntity<Object> responseEntity = globalExceptionHandler.handlePathVariableErrors(ex);

        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        CustomError actualError = (CustomError) responseEntity.getBody();
        assertThat(actualError.getHeader()).isEqualTo(CustomError.Header.VALIDATION_ERROR.getName());
        assertThat(actualError.getSubErrors().get(0).getField()).isEqualTo("field");
    }

    @Test
    void givenRuntimeException_whenHandled_thenRespondWithNotFound() {
        // Given
        RuntimeException ex = new RuntimeException("Generic runtime error");

        // When
        ResponseEntity<?> responseEntity = globalExceptionHandler.handleRuntimeException(ex);

        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        CustomError actualError = (CustomError) responseEntity.getBody();
        assertThat(actualError.getMessage()).isEqualTo("Generic runtime error");
        assertThat(actualError.getHeader()).isEqualTo(CustomError.Header.API_ERROR.getName());
    }

    @Test
    void givenProductAlreadyExistException_whenHandled_thenRespondWithConflict() {
        String errorMessage = "A product with name 'Gadget X' already exists.";
        ProductAlreadyExistException ex = new ProductAlreadyExistException(errorMessage);

        ResponseEntity<CustomError> responseEntity = globalExceptionHandler.handleProductAlreadyExistException(ex);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(responseEntity.getBody().getMessage()).isEqualTo(errorMessage);
        assertThat(responseEntity.getBody().getHeader()).isEqualTo(CustomError.Header.ALREADY_EXIST.getName());
    }

    @Test
    void givenProductNotFoundException_whenHandled_thenRespondWithNotFound() {
        // Given
        ProductNotFoundException ex = new ProductNotFoundException("Product not found!");

        // When
        ResponseEntity<CustomError> responseEntity = globalExceptionHandler.handleProductNotFoundException(ex);

        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        CustomError actualError = responseEntity.getBody();
        assertThat(actualError.getMessage()).isEqualTo("Product not found!");
        assertThat(actualError.getHeader()).isEqualTo(CustomError.Header.NOT_FOUND.getName());
    }

    // --- THIS IS THE NEW, MISSING TEST ---
    @Test
    void givenInsufficientStockException_whenHandled_thenRespondWithConflict() {
        // Given
        InsufficientStockException ex = new InsufficientStockException("Not enough items in stock.");

        // When
        ResponseEntity<CustomError> responseEntity = globalExceptionHandler.handleInsufficientStockException(ex);

        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        CustomError actualError = responseEntity.getBody();
        assertThat(actualError.getMessage()).isEqualTo("Not enough items in stock.");
        assertThat(actualError.getHeader()).isEqualTo("INSUFFICIENT_STOCK");
    }

    @Test
    void givenAuthorizationDeniedException_whenHandled_thenRespondWithForbidden() {
        // Given: THIS IS THE FIX. We must provide a non-null AuthorizationDecision.
        String accessDeniedMessage = "Access is denied";
        AuthorizationDecision decision = new AuthorizationDecision(false); // A simple "denied" decision
        AuthorizationDeniedException ex = new AuthorizationDeniedException(accessDeniedMessage, decision);

        // When
        ResponseEntity<CustomError> responseEntity = globalExceptionHandler.handleAuthorizationDeniedException(ex);

        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(responseEntity.getBody().getHeader()).isEqualTo(CustomError.Header.AUTH_ERROR.getName());
        // The message is now taken from the handler, not the exception itself.
        assertThat(responseEntity.getBody().getMessage()).isEqualTo("Access Denied: You do not have sufficient privileges for this resource.");
    }

    // You can remove the complex checkCustomError helper method as the direct assertions are clearer.
}