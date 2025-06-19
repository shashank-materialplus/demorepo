package com.springbootmicroservices.orderservice.model.order.dto.response;

import com.springbootmicroservices.orderservice.model.order.enums.OrderStatus; // Import your enum
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderHistoryItemResponse {
    private String id;
    private LocalDateTime orderDate; // Could be createdAt from OrderEntity
    private BigDecimal totalAmount;
    private OrderStatus status;
    private Integer itemCount; // Calculated: sum of quantities of all items in the order
}