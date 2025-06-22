package com.springbootmicroservices.orderservice.controller;

import com.springbootmicroservices.orderservice.model.common.dto.response.CustomResponse;
import com.springbootmicroservices.orderservice.model.order.dto.request.PaymentRequestDto;
import com.springbootmicroservices.orderservice.model.order.dto.response.PaymentResponse;
import com.springbootmicroservices.orderservice.model.order.dto.request.CheckoutRequestDto;
import com.springbootmicroservices.orderservice.model.order.dto.response.CheckoutResponseDto;
import com.springbootmicroservices.orderservice.service.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/process")
    public CustomResponse<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequestDto paymentRequest) {
        log.info("PaymentController :: Received request to process payment for Order ID: {}", paymentRequest.getOrderId());
        PaymentResponse paymentResponse = paymentService.processPayment(paymentRequest);
        log.info("PaymentController :: Payment processing response for Order ID: {}. Status: {}",
                paymentRequest.getOrderId(),
                paymentResponse != null ? paymentResponse.getStatus() : "N/A");
        return CustomResponse.successOf(paymentResponse);
    }

    // This endpoint needs to be public in SecurityConfig, as Stripe sends an unauthenticated request.
    // Security is handled by verifying the Stripe-Signature header in PaymentServiceImpl.
    /*
    @PostMapping("/stripe/webhook")
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload,
                                                      @RequestHeader("Stripe-Signature") String sigHeader) {
        log.info("PaymentController :: Received Stripe webhook.");
        try {
            paymentService.handleStripeWebhook(payload, sigHeader);
            log.info("PaymentController :: Stripe webhook processed successfully by service.");
            return ResponseEntity.ok("Webhook received and acknowledged.");
        } catch (Exception e) {
            log.error("PaymentController :: Error processing Stripe webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook processing error: " + e.getMessage());
        }
    }
    */

    @PostMapping("/create-checkout-session")
    public CustomResponse<CheckoutResponseDto> createCheckoutSession(@Valid @RequestBody CheckoutRequestDto checkoutRequest) {
        log.info("PaymentController :: Received request to create Stripe Checkout Session for Order ID: {}", checkoutRequest.getOrderId());
        String sessionUrl = paymentService.createStripeCheckoutSession(checkoutRequest.getOrderId());
        CheckoutResponseDto response = CheckoutResponseDto.builder()
                .redirectUrl(sessionUrl)
                .build();
        return CustomResponse.successOf(response);
    }
}
