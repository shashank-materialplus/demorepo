// src/main/java/com/springbootmicroservices/orderservice/client/dto/PurchaseQuantityDto.java
package com.springbootmicroservices.orderservice.client.dto;

import jakarta.validation.constraints.Min; // If you intend to validate this DTO before sending (though Feign usually doesn't trigger bean validation on client side)
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO used by OrderService to request a stock reduction from ProductService
 * for a specific quantity of a product. This should match the structure
 * expected by ProductService's purchase/stock reduction endpoint.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseQuantityDto {

    @Min(value = 1, message = "Quantity to purchase must be at least 1") // Validation for internal consistency
    private int quantity;
}