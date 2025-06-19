package com.springbootmicroservices.orderservice.model.order.dto.response;

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
public class OrderItemResponse {
    private String productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice; // Price at which it was sold
    private BigDecimal totalPrice; // quantity * unitPrice
}