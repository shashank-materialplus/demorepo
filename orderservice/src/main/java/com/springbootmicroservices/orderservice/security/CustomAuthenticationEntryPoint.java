package com.springbootmicroservices.orderservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
// Ensure this path correctly points to your CustomError DTO within orderservice
import com.springbootmicroservices.orderservice.model.common.CustomError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;

@Component // Makes it a Spring-managed bean
@Slf4j     // For logging
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // Static initializer block to configure the ObjectMapper once
    static {
        OBJECT_MAPPER.registerModule(new JavaTimeModule()); // For serializing Java 8 time types like LocalDateTime
        // You could add other global ObjectMapper configurations here if needed,
        // e.g., objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        // Log the unauthorized access attempt with details
        log.warn("Unauthorized access attempt to API endpoint: {} - Authentication failed: {} (Type: {})",
                request.getRequestURI(),
                authException.getMessage(),
                authException.getClass().getSimpleName());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        // Construct your standard CustomError response
        CustomError customError = CustomError.builder()
                .httpStatus(HttpStatus.UNAUTHORIZED)
                // Ensure CustomError.Header enum and its AUTH_ERROR value are correctly defined and accessible
                .header(CustomError.Header.AUTH_ERROR.getName())
                .message(authException.getMessage() != null ? authException.getMessage() : "Authentication is required to access this resource.")
                .isSuccess(false)
                // You could add subErrors or other details here if the authException provides them
                // and if CustomError supports them. For a basic 401, the message is often sufficient.
                .build();

        // Write the CustomError object as JSON to the response output stream
        try (OutputStream out = response.getOutputStream()) {
            OBJECT_MAPPER.writeValue(out, customError);
            // No need to explicitly flush if using try-with-resources for OutputStream,
            // as close() will typically flush. However, explicit flush() doesn't hurt.
            // out.flush();
        } catch (IOException e) {
            // This catch block is for errors during the writing of the error response itself.
            // The original authException has already occurred.
            log.error("Critical error: Could not write the 401 unauthorized response to the output stream. Original auth error for {}: {}",
                    request.getRequestURI(), authException.getMessage(), e);

            // If the response hasn't been committed yet, try to send a generic server error.
            // This is a last resort.
            if (!response.isCommitted()) {
                response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Server error while attempting to send authentication error response.");
            }
        }
    }
}