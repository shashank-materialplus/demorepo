package com.springbootmicroservices.orderservice.service;

import com.springbootmicroservices.orderservice.model.order.dto.request.PaymentRequestDto;
import com.springbootmicroservices.orderservice.model.order.dto.response.PaymentResponse;

public interface PaymentService {

    /**
     * Processes a payment for a given order using a payment method ID (e.g., from Stripe).
     * This typically involves creating/confirming a PaymentIntent with Stripe.
     *
     * @param paymentRequest The request containing orderId and paymentMethodId.
     * @return A PaymentResponse indicating the outcome (success, requires_action, failed)
     *         and potentially a clientSecret for frontend Stripe.js handling.
     */
    PaymentResponse processPayment(PaymentRequestDto paymentRequest);

    String createStripeCheckoutSession(String orderId); // -- new -->

    /**
     * Handles incoming webhook events from Stripe.
     *
     * @param payload The raw JSON payload from Stripe.
     * @param sigHeader The 'Stripe-Signature' header value.
     */
    // void handleStripeWebhook(String payload, String sigHeader);
}
