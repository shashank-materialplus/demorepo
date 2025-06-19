package com.springbootmicroservices.orderservice.model.common.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@Builder
public class CustomResponse<T> {

    @Builder.Default
    private LocalDateTime time = LocalDateTime.now();

    private HttpStatus httpStatus;

    private Boolean isSuccess;

    @JsonInclude(JsonInclude.Include.NON_NULL) // Only include 'response' field if it's not null
    private T response;

    public static final CustomResponse<Void> SUCCESS = CustomResponse.<Void>builder()
            .httpStatus(HttpStatus.OK)
            .isSuccess(true)
            .build();

    public static <T> CustomResponse<T> successOf(final T response) {
        return CustomResponse.<T>builder()
                .httpStatus(HttpStatus.OK)
                .isSuccess(true)
                .response(response)
                .build();
    }

    // Optional: Method for creating error responses if not handled solely by CustomError + GlobalExceptionHandler
    public static <T> CustomResponse<T> error(HttpStatus status, T errorDetails) {
        return CustomResponse.<T>builder()
                .httpStatus(status)
                .isSuccess(false)
                .response(errorDetails) // 'errorDetails' would be your CustomError object
                .build();
    }
}