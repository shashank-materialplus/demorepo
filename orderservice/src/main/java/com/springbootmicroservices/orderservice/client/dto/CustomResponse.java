// src/main/java/com/springbootmicroservices/orderservice/client/dto/CustomResponse.java
package com.springbootmicroservices.orderservice.client.dto;

import lombok.Data; // Using @Data for simplicity for this client DTO
import java.time.LocalDateTime;

@Data // Includes @Getter, @Setter, @ToString, @EqualsAndHashCode
public class CustomResponse<T> {
    private LocalDateTime time;
    private String httpStatus; // String, as HttpStatus enum might not deserialize easily
    private Boolean isSuccess;
    private T response; // This will hold the ProductDetailsDto
}