package com.springbootmicroservices.orderservice.model.order.dto.response;

// Re-use ShippingAddressDto for response if the structure is the same
import com.springbootmicroservices.orderservice.model.order.dto.request.ShippingAddressDto;
import com.springbootmicroservices.orderservice.model.order.enums.OrderStatus; // Import your enum
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private String id;
    private String userId;
    private List<OrderItemResponse> items;
    private BigDecimal totalAmount;
    private OrderStatus orderStatus;
    private ShippingAddressDto shippingAddress; // Assuming ShippingAddressDto is suitable for response
    private String externalPaymentId; // Renamed from paymentId for clarity
    private String stripePaymentIntentId;
    private String stripeClientSecret; // Only if needed by client for next steps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String orderNotes; // If you added this to OrderEntity
}