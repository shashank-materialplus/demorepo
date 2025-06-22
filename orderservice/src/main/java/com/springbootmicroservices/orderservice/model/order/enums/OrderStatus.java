package com.springbootmicroservices.orderservice.model.order.enums;

public enum OrderStatus {
    PENDING,
    CANCELLED,
    PENDING_PAYMENT,    // Order created, awaiting payment confirmation (e.g., from Stripe)
    PAYMENT_FAILED,     // Payment attempt was made but failed
    PENDING_CONFIRMATION, // Payment successful, order details being finalized/stock allocated (optional intermediate state)
    CONFIRMED,          // Order confirmed, payment successful, ready for processing
    PROCESSING,         // Order is being prepared for shipment
    SHIPPED,            // Order has been shipped
    DELIVERED,          // Order has been delivered to the customer
    CANCELLED_BY_USER,  // Order cancelled by the user
    CANCELLED_BY_ADMIN, // Order cancelled by an administrator
    RETURN_REQUESTED,   // User has requested a return
    RETURN_APPROVED,    // Return request approved
    RETURNED,           // Items have been returned
    REFUNDED;           // Order has been refunded

    // You can add methods here if needed, e.g., to check if an order is in a final state
    public boolean isFinalState() {
        return this == DELIVERED || this == CANCELLED_BY_USER || this == CANCELLED_BY_ADMIN || this == REFUNDED || this == PAYMENT_FAILED;
    }

    public boolean canBeCancelledByUser() {
        return this == PENDING_PAYMENT || this == PENDING_CONFIRMATION || this == CONFIRMED;
    }
}