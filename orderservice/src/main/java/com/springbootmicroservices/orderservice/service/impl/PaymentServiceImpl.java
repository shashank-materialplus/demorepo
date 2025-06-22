package com.springbootmicroservices.orderservice.service.impl;

import com.springbootmicroservices.orderservice.exception.OrderNotFoundException;
import com.springbootmicroservices.orderservice.exception.PaymentProcessingException;
import com.springbootmicroservices.orderservice.model.order.dto.request.PaymentRequestDto;
import com.springbootmicroservices.orderservice.model.order.dto.response.PaymentResponse;
import com.springbootmicroservices.orderservice.model.order.entity.OrderEntity;
import com.springbootmicroservices.orderservice.repository.OrderRepository;
import com.springbootmicroservices.orderservice.service.PaymentService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;

    // The Stripe secret key is now hardcoded as requested.
    private final String stripeSecretKey = "sk_test_51RZqPfBOUTLq4QBi4uRppN7be87FwTSjizRbUYw6qF5aZKUk38Uv5BKNJqlVLxQZAW0FQgVkWYrdckRp0yKfCAoS00rphhsI4z";

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    // --- THIS IS THE MISSING METHOD THAT IS NOW ADDED ---
    // It must exist to fulfill the contract of the PaymentService interface.
    @Override
    public PaymentResponse processPayment(PaymentRequestDto paymentRequest) {
        log.warn("The 'processPayment' method was called but is not implemented for the Stripe redirect flow.");
        // Throwing an exception is better than returning null, as it indicates this path is not supported.
        throw new UnsupportedOperationException("The 'processPayment' method is not supported in this implementation.");
    }
    // --- END OF ADDED METHOD ---

    @Override
    public String createStripeCheckoutSession(String orderId) {
        // 1. Fetch the order from your database
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));

        // 2. Define the URLs Stripe will redirect to after payment
        String successUrl = "http://localhost:5173/payment/success?session_id={CHECKOUT_SESSION_ID}";
        String cancelUrl = "http://localhost:5173/payment/cancel";

        // 3. Create a list of line items for Stripe
        List<SessionCreateParams.LineItem> lineItems = order.getItems().stream()
                .map(item -> SessionCreateParams.LineItem.builder()
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("usd") // Set to Indian Rupees
                                .setUnitAmount(item.getUnitPrice().multiply(new BigDecimal("100")).longValue())
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName(item.getProductName())
                                        .build())
                                .build())
                        .setQuantity(Long.valueOf(item.getQuantity()))
                        .build())
                .collect(Collectors.toList());

        // 4. Build the session parameters
        SessionCreateParams params = SessionCreateParams.builder()
                .addAllLineItem(lineItems)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .putMetadata("orderId", order.getId())
                .build();

        try {
            // 5. Create the session and get the redirect URL
            Session session = Session.create(params);
            return session.getUrl();
        } catch (StripeException e) {
            log.error("Error creating Stripe session for order {}: {}", orderId, e.getMessage(), e);
            throw new PaymentProcessingException("Could not create Stripe payment session.");
        }
    }
}
