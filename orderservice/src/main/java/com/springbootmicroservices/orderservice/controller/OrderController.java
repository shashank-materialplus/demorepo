package com.springbootmicroservices.orderservice.controller;

import com.springbootmicroservices.orderservice.model.common.dto.response.CustomResponse;
import com.springbootmicroservices.orderservice.model.common.dto.response.CustomPagingResponse; // For admin/all if paginated
import com.springbootmicroservices.orderservice.model.order.dto.request.CreateOrderRequest;
import com.springbootmicroservices.orderservice.model.order.dto.request.UpdateOrderStatusRequest;
import com.springbootmicroservices.orderservice.model.order.dto.response.OrderHistoryItemResponse;
import com.springbootmicroservices.orderservice.model.order.dto.response.OrderResponse;
import com.springbootmicroservices.orderservice.model.order.mapper.OrderMapper; // If using mapper for paging response
import com.springbootmicroservices.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page; // If service returns Page
import org.springframework.data.domain.Pageable; // For paginated admin endpoint
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;
    // private final OrderMapper orderMapper; // Only if you need to map Page to CustomPagingResponse here

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomResponse<OrderResponse> placeOrder(@Valid @RequestBody CreateOrderRequest createOrderRequest) {
        log.info("OrderController :: Received request to place a new order with {} items.",
                createOrderRequest.getItems() != null ? createOrderRequest.getItems().size() : 0);
        OrderResponse orderResponse = orderService.createOrder(createOrderRequest);
        log.info("OrderController :: Order placed successfully with ID: {}", orderResponse.getId());
        return CustomResponse.<OrderResponse>builder()
                .httpStatus(HttpStatus.CREATED)
                .isSuccess(true)
                .response(orderResponse)
                .build();
    }

    @GetMapping("/history")
    public CustomResponse<List<OrderHistoryItemResponse>> getOrderHistory() {
        log.info("OrderController :: Received request to fetch order history for current user.");
        List<OrderHistoryItemResponse> history = orderService.getOrderHistoryForCurrentUser();
        return CustomResponse.successOf(history);
    }

    @GetMapping("/{orderId}")
    public CustomResponse<OrderResponse> getOrderById(@PathVariable String orderId) {
        log.info("OrderController :: Received request to fetch order by ID: {}", orderId);
        OrderResponse order = orderService.getOrderByIdForCurrentUser(orderId);
        return CustomResponse.successOf(order);
    }

    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')") // Corrected to ROLE_ADMIN based on TokenServiceImpl
    // Or @PreAuthorize("hasRole('ADMIN')")
    public CustomResponse<OrderResponse> updateOrderStatus(
            @PathVariable String orderId,
            @Valid @RequestBody UpdateOrderStatusRequest updateOrderStatusRequest) {
        log.info("OrderController :: Received ADMIN request to update status for order ID: {} to {}",
                orderId, updateOrderStatusRequest.getNewStatus());
        OrderResponse updatedOrder = orderService.updateOrderStatus(orderId, updateOrderStatusRequest.getNewStatus());
        log.info("OrderController :: Order ID: {} status updated successfully to {}", orderId, updatedOrder.getOrderStatus());
        return CustomResponse.successOf(updatedOrder);
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public CustomResponse<CustomPagingResponse<OrderResponse>> getAllOrdersForAdmin(Pageable pageable) {
        log.info("OrderController :: Received ADMIN request to fetch all orders with pagination: {}", pageable);

        Page<OrderResponse> orderResponsePage = orderService.getAllOrdersForAdmin(pageable);

        // Map Spring Data Page to your CustomPagingResponse
        CustomPagingResponse<OrderResponse> pagingResponse = CustomPagingResponse.<OrderResponse>builder()
                .content(orderResponsePage.getContent())
                .pageNumber(orderResponsePage.getNumber() + 1) // Spring Page is 0-indexed, client usually expects 1-indexed
                .pageSize(orderResponsePage.getSize())
                .totalElementCount(orderResponsePage.getTotalElements())
                .totalPageCount(orderResponsePage.getTotalPages())
                .build();

        return CustomResponse.successOf(pagingResponse);
    }
}