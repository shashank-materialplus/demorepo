// src/main/java/com/springbootmicroservices/orderservice/client/dto/ProductDetailsDto.java
package com.springbootmicroservices.orderservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO representing the essential details of a product fetched from ProductService,
 * needed for order processing within OrderService.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailsDto {
    private String id;          // Product ID
    private String name;        // Product Name
    private BigDecimal unitPrice; // Current unit price of the product
    private Integer amount;     // Current available stock/amount of the product
    // Add any other fields from ProductService's ProductResponse that OrderService might need
    // For example:
    private String category;

    // private String category;
    // private String imageUrl;
}