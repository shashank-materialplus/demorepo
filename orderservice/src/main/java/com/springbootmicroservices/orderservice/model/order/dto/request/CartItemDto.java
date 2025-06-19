package com.springbootmicroservices.orderservice.model.order.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemDto {

    @NotBlank(message = "Product ID cannot be blank")
    private String productId;

    @NotBlank(message = "Product name cannot be blank")
    private String name; // Product name (sent from client, can be re-verified against ProductService)

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Unit price cannot be null")
    @DecimalMin(value = "0.01", message = "Unit price must be positive") // Price per unit
    private BigDecimal unitPrice;
}