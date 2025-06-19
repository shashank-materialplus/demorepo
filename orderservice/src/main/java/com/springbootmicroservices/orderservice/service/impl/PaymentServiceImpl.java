package com.springbootmicroservices.orderservice.service.impl;

import com.google.gson.JsonSyntaxException; // Stripe SDK uses Gson for JSON parsing internally
import com.springbootmicroservices.orderservice.exception.OrderNotFoundException;
import com.springbootmicroservices.orderservice.exception.PaymentProcessingException;
import com.springbootmicroservices.orderservice.exception.InvalidOrderStatusException;
import com.springbootmicroservices.orderservice.model.auth.enums.TokenClaims; // For USER_ID
import com.springbootmicroservices.orderservice.model.order.dto.request.PaymentRequestDto;
import com.springbootmicroservices.orderservice.model.order.dto.response.PaymentResponse;
import com.springbootmicroservices.orderservice.model.order.entity.OrderEntity;
import com.springbootmicroservices.orderservice.model.order.enums.OrderStatus;
import com.springbootmicroservices.orderservice.repository.OrderRepository;
import com.springbootmicroservices.orderservice.service.PaymentService;
import org.springframework.beans.factory.annotation.Value;
// TokenService is not strictly needed here if getCurrentUserId directly extracts from SecurityContext
// import com.springbootmicroservices.orderservice.service.TokenService;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer; // Correct class for deserializing
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject; // Base class for Stripe objects in events
import com.stripe.param.PaymentIntentCreateParams;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;
    // private final TokenService tokenService; // Not directly used in getCurrentUserId logic here

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    @Value("${stripe.payment.return-url}")
    private String stripeReturnUrl;

    //@Value("${stripe.webhook-secret}")
    //private String stripeWebhookSecret;

    @PostConstruct
    public void init() {
        // Check if keys are loaded, Stripe.apiKey assignment will fail if stripeSecretKey is null
        if (this.stripeSecretKey == null || this.stripeSecretKey.isBlank() || this.stripeSecretKey.startsWith("${")) {
            log.error("CRITICAL: Stripe Secret Key is not configured correctly! Value: {}", this.stripeSecretKey);
            // You might want to throw an exception here to prevent the app from starting in a broken state
            // throw new IllegalStateException("Stripe Secret Key (stripe.secret-key) is not configured. Check environment variables or application.yml defaults.");
        } else {
            Stripe.apiKey = this.stripeSecretKey;
            log.info("Stripe API Key configured.");
        }

//        if (this.stripeWebhookSecret == null || this.stripeWebhookSecret.isBlank() || this.stripeWebhookSecret.startsWith("${")) {
//            log.warn("Stripe Webhook Signing Secret ('stripe.webhook-secret') is NOT configured correctly! Value: {}. Webhook signature verification will fail.", this.stripeWebhookSecret);
//        } else {
//            log.info("Stripe Webhook Secret configured.");
//        }
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String userId = jwt.getClaimAsString(TokenClaims.USER_ID.getValue());
            if (userId == null || userId.isBlank()) {
                log.error("USER_ID claim missing or blank from JWT token for principal: {}", jwt.getSubject());
                throw new IllegalStateException("User ID could not be determined from JWT token for payment.");
            }
            return userId;
        }
        log.warn("Could not extract JWT principal from SecurityContext during payment. Authentication: {}", authentication);
        throw new IllegalStateException("User ID could not be determined from security context for payment processing.");
    }

    @Override
    @Transactional
    public PaymentResponse processPayment(PaymentRequestDto paymentRequest) {
        String currentUserId = getCurrentUserId();
        log.info("PaymentServiceImpl :: Processing payment for Order ID: {} by User ID: {}", paymentRequest.getOrderId(), currentUserId);

        OrderEntity order = orderRepository.findById(paymentRequest.getOrderId())
                .orElseThrow(() -> {
                    log.warn("PaymentServiceImpl :: Order not found with ID: {}", paymentRequest.getOrderId());
                    return new OrderNotFoundException("Order not found with ID: " + paymentRequest.getOrderId());
                });

        if (!order.getUserId().equals(currentUserId)) {
            log.warn("PaymentServiceImpl :: User ID mismatch. Order User: {}, Current User: {}. Order ID: {}", order.getUserId(), currentUserId, order.getId());
            throw new PaymentProcessingException("You do not have permission to pay for this order.");
        }

        if (order.getOrderStatus() != OrderStatus.PENDING_PAYMENT) {
            log.warn("PaymentServiceImpl :: Attempt to pay for Order ID: {} not in PENDING_PAYMENT state. Current status: {}", order.getId(), order.getOrderStatus());
            throw new InvalidOrderStatusException("Order is not in a state that allows payment. Current status: " + order.getOrderStatus());
        }

        // Check if a PaymentIntent already exists and is in a final state
        if (order.getStripePaymentIntentId() != null) {
            try {
                PaymentIntent existingPI = PaymentIntent.retrieve(order.getStripePaymentIntentId());
                log.info("PaymentServiceImpl :: Existing PaymentIntent {} found for Order ID: {}. Status: {}", existingPI.getId(), order.getId(), existingPI.getStatus());
                // If PI already succeeded or is processing, don't create a new one.
                if ("succeeded".equals(existingPI.getStatus()) || "processing".equals(existingPI.getStatus())) {
                    return handlePaymentIntentStatus(order, existingPI);
                }
                // If requires_action or requires_payment_method, we might still allow client to retry with same PI if clientSecret is known
                if (("requires_action".equals(existingPI.getStatus()) || "requires_payment_method".equals(existingPI.getStatus())) && existingPI.getClientSecret() != null) {
                    return handlePaymentIntentStatus(order, existingPI); // Return existing details
                }
                // If it's 'canceled' or truly 'failed', it might be okay to create a new one,
                // or you might want stricter rules (e.g., user must create a new order).
                // For now, we'll proceed to create a new one if not succeeded/processing/actionable.
            } catch (StripeException e) {
                log.error("PaymentServiceImpl :: Stripe error retrieving existing PaymentIntent {} for Order ID {}: {}", order.getStripePaymentIntentId(), order.getId(), e.getMessage());
                // Proceed to create a new one if retrieval fails, assuming the old one is problematic.
            }
        }

        try {
            PaymentIntentCreateParams createParams = PaymentIntentCreateParams.builder()
                    .setAmount(order.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValueExact())
                    .setCurrency("usd") // Make this configurable
                    .setPaymentMethod(paymentRequest.getPaymentMethodId())
                    .setConfirmationMethod(PaymentIntentCreateParams.ConfirmationMethod.MANUAL)
                    .setConfirm(true)
                    .setReturnUrl(this.stripeReturnUrl + "?order_id=" + order.getId())
                    .putMetadata("order_id", order.getId())
                    .putMetadata("user_id", order.getUserId())
                    //.setErrorOnRequiresAction(true) // Will throw an exception if immediate confirmation leads to requires_action
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(createParams);
            log.info("PaymentServiceImpl :: Stripe PaymentIntent created/confirmed: {} for Order ID: {}", paymentIntent.getId(), order.getId());
            return handlePaymentIntentStatus(order, paymentIntent);

        } catch (StripeException e) { // Catches API errors, card errors etc.
            log.error("PaymentServiceImpl :: Stripe error during payment processing for Order ID {}: {}", order.getId(), e.getMessage(), e);
            order.setOrderStatus(OrderStatus.PAYMENT_FAILED);
            // Potentially store Stripe error code/message on order
            orderRepository.save(order);
            // Provide a more specific message if possible from e.getStripeError().getMessage()
            throw new PaymentProcessingException("Payment failed: " + (e.getStripeError() != null ? e.getStripeError().getMessage() : e.getMessage()), e);
        } catch (Exception e) {
            log.error("PaymentServiceImpl :: Unexpected error during payment for Order ID {}: {}", order.getId(), e.getMessage(), e);
            order.setOrderStatus(OrderStatus.PAYMENT_FAILED);
            orderRepository.save(order);
            throw new PaymentProcessingException("An unexpected error occurred during payment processing.", e);
        }
    }

    private PaymentResponse handlePaymentIntentStatus(OrderEntity order, PaymentIntent paymentIntent) {
        log.debug("Handling PaymentIntent status: {} for Order ID: {}", paymentIntent.getStatus(), order.getId());
        PaymentResponse.PaymentResponseBuilder responseBuilder = PaymentResponse.builder()
                .orderId(order.getId())
                .paymentIntentId(paymentIntent.getId())
                .status(paymentIntent.getStatus());

        boolean needsSave = false;

        // Store PI ID and Client Secret on the order if not already set or if they change
        if (!Objects.equals(order.getStripePaymentIntentId(), paymentIntent.getId())) {
            order.setStripePaymentIntentId(paymentIntent.getId());
            needsSave = true;
        }
        if (paymentIntent.getClientSecret() != null && !Objects.equals(order.getStripeClientSecret(), paymentIntent.getClientSecret())) {
            order.setStripeClientSecret(paymentIntent.getClientSecret());
            needsSave = true;
        }


        switch (paymentIntent.getStatus()) {
            case "succeeded":
                log.info("Payment succeeded for Order ID: {}. PaymentIntent ID: {}", order.getId(), paymentIntent.getId());
                if (order.getOrderStatus() != OrderStatus.CONFIRMED && order.getOrderStatus() != OrderStatus.PROCESSING) {
                    order.setOrderStatus(OrderStatus.CONFIRMED);
                    order.setExternalPaymentId(paymentIntent.getId()); // Or paymentIntent.getLatestCharge() if available and preferred
                    order.setStripeClientSecret(null); // Not needed anymore by client
                    needsSave = true;
                }
                responseBuilder.message("Payment successful.");
                break;

            case "requires_action":
            case "requires_source_action": // Older API versions might use this
                log.info("Payment requires action for Order ID: {}. PaymentIntent ID: {}", order.getId(), paymentIntent.getId());
                responseBuilder.clientSecret(paymentIntent.getClientSecret()).message("Payment requires further action from the user.");
                // Order status remains PENDING_PAYMENT or a specific AWAITING_USER_ACTION
                break;

            case "requires_payment_method": // Card declined, or general failure before success
                log.warn("Payment requires new payment method for Order ID: {}. PaymentIntent ID: {}", order.getId(), paymentIntent.getId());
                if (order.getOrderStatus() != OrderStatus.PAYMENT_FAILED) {
                    order.setOrderStatus(OrderStatus.PAYMENT_FAILED);
                    needsSave = true;
                }
                responseBuilder.message("Payment failed. Please try a different payment method. " + (paymentIntent.getLastPaymentError() != null ? paymentIntent.getLastPaymentError().getMessage() : ""));
                break;

            case "processing":
                log.info("Payment processing for Order ID: {}. PaymentIntent ID: {}", order.getId(), paymentIntent.getId());
                // order.setOrderStatus(OrderStatus.PENDING_CONFIRMATION); // If you have this state
                responseBuilder.message("Payment is currently processing.");
                break;

            case "canceled":
                log.warn("PaymentIntent {} was canceled for Order ID {}.", paymentIntent.getId(), order.getId());
                if (order.getOrderStatus() != OrderStatus.PAYMENT_FAILED && order.getOrderStatus() != OrderStatus.CANCELLED_BY_USER && order.getOrderStatus() != OrderStatus.CANCELLED_BY_ADMIN) {
                    order.setOrderStatus(OrderStatus.PAYMENT_FAILED); // Or a specific CANCELLED_PAYMENT
                    needsSave = true;
                }
                responseBuilder.message("Payment was cancelled.");
                break;

            default: // e.g., "requires_capture" (not typical for this flow)
                log.warn("Payment for Order ID {} ended with unhandled status: {}. PaymentIntent ID: {}", order.getId(), paymentIntent.getStatus(), paymentIntent.getId());
                if (order.getOrderStatus() != OrderStatus.PAYMENT_FAILED) {
                    order.setOrderStatus(OrderStatus.PAYMENT_FAILED);
                    needsSave = true;
                }
                responseBuilder.message("Payment status: " + paymentIntent.getStatus());
                break;
        }

        if (needsSave) {
            orderRepository.save(order);
            log.info("Order ID {} status/details updated based on PaymentIntent status {}", order.getId(), paymentIntent.getStatus());
        }
        return responseBuilder.build();
    }


//    @Override
//    @Transactional
//    public void handleStripeWebhook(String payload, String sigHeader) {
//        if (this.stripeWebhookSecret == null || this.stripeWebhookSecret.isBlank()) {
//            log.error("Stripe webhook secret ('stripe.webhook.secret') is not configured. Cannot verify webhook signature. Skipping event processing.");
//            // IMPORTANT: Do not throw an exception here that would cause Stripe to retry if the issue is config.
//            // Log it as a critical error for ops to fix.
//            return;
//        }
//
//        Event event;
//        try {
//            event = Webhook.constructEvent(payload, sigHeader, this.stripeWebhookSecret);
//        } catch (SignatureVerificationException e) {
//            log.warn("Webhook error: Invalid Stripe signature. Check your webhook signing secret. sigHeader: {}", sigHeader, e);
//            throw new PaymentProcessingException("Invalid Stripe webhook signature.", e); // This will result in 400 to Stripe
//        } catch (JsonSyntaxException e) {
//            log.warn("Webhook error: Invalid JSON payload.", e);
//            throw new PaymentProcessingException("Invalid JSON payload in Stripe webhook.", e); // This will result in 400 to Stripe
//        }
//
//        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
//        StripeObject stripeObject = null;
//        if (dataObjectDeserializer.getObject().isPresent()) {
//            stripeObject = dataObjectDeserializer.getObject().get();
//        } else {
//            log.warn("Webhook error: Could not deserialize event data object for event type: {}. Event ID: {}", event.getType(), event.getId());
//            return; // Acknowledge receipt to Stripe, but can't process further.
//        }
//
//        log.info("PaymentServiceImpl :: Received Stripe Webhook Event: ID = {}, Type = {}, Livemode={}", event.getId(), event.getType(), event.getLivemode());
//
//        PaymentIntent paymentIntent;
//        OrderEntity order;
//
//        switch (event.getType()) {
//            case "payment_intent.succeeded":
//                paymentIntent = (PaymentIntent) stripeObject;
//                log.info("Webhook :: PaymentIntent Succeeded: PI_ID={}", paymentIntent.getId());
//                order = orderRepository.findByStripePaymentIntentId(paymentIntent.getId()).orElse(null);
//                if (order != null) {
//                    // Only update if order is in a state that expects payment confirmation
//                    if (order.getOrderStatus() == OrderStatus.PENDING_PAYMENT ||
//                            order.getOrderStatus() == OrderStatus.PAYMENT_FAILED || // Allow update if it previously failed
//                            order.getOrderStatus() == OrderStatus.PENDING_CONFIRMATION) { // Or similar state
//                        order.setOrderStatus(OrderStatus.CONFIRMED);
//                        order.setExternalPaymentId(paymentIntent.getId());
//                        order.setStripeClientSecret(null); // Clear client secret
//                        orderRepository.save(order);
//                        log.info("Webhook :: Order ID {} status updated to CONFIRMED by PI Succeeded.", order.getId());
//                        // TODO: Trigger fulfillment process (e.g., send event, call notification service)
//                    } else {
//                        log.info("Webhook :: Order ID {} already in status {} or further processed. No action for PI Succeeded.", order.getId(), order.getOrderStatus());
//                    }
//                } else {
//                    log.warn("Webhook :: Succeeded PaymentIntent PI_ID={} received, but no matching order found in DB.", paymentIntent.getId());
//                }
//                break;
//
//            case "payment_intent.payment_failed":
//                paymentIntent = (PaymentIntent) stripeObject;
//                log.warn("Webhook :: PaymentIntent Failed: PI_ID={}, Last Error: {}",
//                        paymentIntent.getId(),
//                        paymentIntent.getLastPaymentError() != null ? paymentIntent.getLastPaymentError().getMessage() : "N/A");
//                order = orderRepository.findByStripePaymentIntentId(paymentIntent.getId()).orElse(null);
//                if (order != null) {
//                    if (order.getOrderStatus() != OrderStatus.PAYMENT_FAILED &&
//                            order.getOrderStatus() != OrderStatus.CANCELLED_BY_USER && // Don't revert a cancelled order
//                            order.getOrderStatus() != OrderStatus.CANCELLED_BY_ADMIN) {
//                        order.setOrderStatus(OrderStatus.PAYMENT_FAILED);
//                        orderRepository.save(order);
//                        log.info("Webhook :: Order ID {} status updated to PAYMENT_FAILED by PI Failed.", order.getId());
//                        // TODO: Notify user of payment failure
//                    } else {
//                        log.info("Webhook :: Order ID {} already in status {}. No action for PI Failed.", order.getId(), order.getOrderStatus());
//                    }
//                } else {
//                    log.warn("Webhook :: Failed PaymentIntent PI_ID={} received, but no matching order found in DB.", paymentIntent.getId());
//                }
//                break;
//
//            // Add more event types as needed, e.g., for refunds
//            // case "charge.refunded":
//            //     Charge charge = (Charge) stripeObject;
//            //     log.info("Webhook :: Charge Refunded: Charge_ID={}, PI_ID={}", charge.getId(), charge.getPaymentIntent());
//            //     // Find order by paymentIntent or chargeId and update status to REFUNDED
//            //     break;
//
//            default:
//                log.info("Webhook :: Unhandled event type: {}", event.getType());
//        }
//    }
}