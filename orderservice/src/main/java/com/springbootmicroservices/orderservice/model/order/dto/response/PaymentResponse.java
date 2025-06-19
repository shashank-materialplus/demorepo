package com.springbootmicroservices.orderservice.model.order.dto.response;

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
public class PaymentResponse {
    private String orderId;
    private String paymentIntentId; // Stripe's Payment Intent ID
    private String clientSecret;    // If payment requires further client-side action (e.g., 3D Secure)
    private String status;          // e.g., "succeeded", "requires_action", "processing", "failed"
    private String message;         // Optional message, e.g., success or failure reason
}