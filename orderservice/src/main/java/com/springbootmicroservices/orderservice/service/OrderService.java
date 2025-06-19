package com.springbootmicroservices.orderservice.service;

import com.springbootmicroservices.orderservice.model.order.dto.request.CreateOrderRequest;
import com.springbootmicroservices.orderservice.model.order.dto.response.OrderHistoryItemResponse;
import com.springbootmicroservices.orderservice.model.order.dto.response.OrderResponse;
import com.springbootmicroservices.orderservice.model.order.enums.OrderStatus;
import org.springframework.data.domain.Page; // Alternative: Spring Data Page
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {

    /**
     * Creates a new order based on the provided request.
     * This involves validating products, checking stock (via ProductService),
     * calculating totals, creating order and order items, and reducing stock.
     *
     * @param createOrderRequest The request containing cart items and shipping address.
     * @return The created OrderResponse, typically including an orderId and status like PENDING_PAYMENT.
     */
    OrderResponse createOrder(CreateOrderRequest createOrderRequest);

    /**
     * Retrieves the order history for the currently authenticated user.
     *
     * @return A list of order summaries.
     */
    List<OrderHistoryItemResponse> getOrderHistoryForCurrentUser();

    /**
     * Retrieves a specific order by its ID for the currently authenticated user.
     * Ensures the user owns the order or is an admin.
     *
     * @param orderId The ID of the order to retrieve.
     * @return The full OrderResponse.
     */
    OrderResponse getOrderByIdForCurrentUser(String orderId);


    /**
     * Updates the status of an existing order. (Admin only)
     *
     * @param orderId The ID of the order to update.
     * @param newStatus The new status for the order.
     * @return The updated OrderResponse.
     */
    OrderResponse updateOrderStatus(String orderId, OrderStatus newStatus);

    // Optional: Method specifically for admin to get any order
    // OrderResponse getOrderByIdForAdmin(String orderId);
    Page<OrderResponse> getAllOrdersForAdmin(Pageable pageable);

}