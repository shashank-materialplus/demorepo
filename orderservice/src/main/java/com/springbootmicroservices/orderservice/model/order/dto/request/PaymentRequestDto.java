package com.springbootmicroservices.orderservice.model.order.dto.request;

import jakarta.validation.constraints.NotBlank;
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
public class PaymentRequestDto {

    @NotBlank(message = "Order ID cannot be blank")
    private String orderId;

    @NotBlank(message = "Payment method ID cannot be blank")
    private String paymentMethodId; // From Stripe Elements (e.g., "pm_...") or similar payment token

    // Optional: could include amount for verification, though backend should use order's amount
    // private BigDecimal amount;
}