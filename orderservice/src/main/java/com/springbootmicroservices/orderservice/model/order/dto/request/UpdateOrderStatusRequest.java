package com.springbootmicroservices.orderservice.model.order.dto.request;

import com.springbootmicroservices.orderservice.model.order.enums.OrderStatus; // Import your enum
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateOrderStatusRequest {

    @NotNull(message = "New status cannot be null")
    private OrderStatus newStatus; // Use your OrderStatus enum
}