package com.springbootmicroservices.orderservice.controller;

import com.springbootmicroservices.orderservice.model.common.dto.response.CustomResponse;
import com.springbootmicroservices.orderservice.model.order.dto.request.PaymentRequestDto;
import com.springbootmicroservices.orderservice.model.order.dto.response.PaymentResponse;
import com.springbootmicroservices.orderservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus; // For ResponseEntity status code
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
                paymentResponse != null ? paymentResponse.getStatus() : "N/A"); // Null check for safety
        return CustomResponse.successOf(paymentResponse);
    }

    // This endpoint needs to be public in SecurityConfig, as Stripe sends an unauthenticated request.
    // Security is handled by verifying the Stripe-Signature header in PaymentServiceImpl.
//    @PostMapping("/stripe/webhook")
//    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
//        log.info("PaymentController :: Received Stripe webhook.");
//        try {
//            paymentService.handleStripeWebhook(payload, sigHeader);
//            log.info("PaymentController :: Stripe webhook processed successfully by service.");
//            return ResponseEntity.ok("Webhook received and acknowledged.");
//        } catch (Exception e) { // Catching general exceptions from service layer (e.g., PaymentProcessingException)
//            log.error("PaymentController :: Error processing Stripe webhook: {}", e.getMessage(), e);
//            // For Stripe webhooks, it's often recommended to return 200 OK even if your internal processing
//            // fails after validating the signature, to prevent Stripe from retrying indefinitely.
//            // Handle the internal failure (log, alert, queue for retry).
//            // If the exception is due to invalid signature (thrown by PaymentService),
//            // then a 400 is appropriate.
//            // This depends on how PaymentService.handleStripeWebhook throws exceptions.
//            // If PaymentProcessingException for signature is thrown, GlobalExceptionHandler might handle it.
//            // For now, a general bad request for any error from service:
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook processing error: " + e.getMessage());
//        }
//    }
}