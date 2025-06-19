package com.springbootmicroservices.orderservice.service.impl;

import com.springbootmicroservices.orderservice.client.ProductServiceClient;
import com.springbootmicroservices.orderservice.client.dto.ProductDetailsDto;
import com.springbootmicroservices.orderservice.client.dto.PurchaseQuantityDto;
import com.springbootmicroservices.orderservice.client.dto.CustomResponse; // Assuming this is your client-side DTO for the wrapper
import com.springbootmicroservices.orderservice.exception.InsufficientStockException;
import com.springbootmicroservices.orderservice.exception.InvalidOrderStatusException;
import com.springbootmicroservices.orderservice.exception.OrderNotFoundException;
import com.springbootmicroservices.orderservice.model.auth.enums.TokenClaims;
import com.springbootmicroservices.orderservice.model.order.dto.request.CartItemDto;
import com.springbootmicroservices.orderservice.model.order.dto.request.CreateOrderRequest;
import com.springbootmicroservices.orderservice.model.order.dto.response.OrderHistoryItemResponse;
import com.springbootmicroservices.orderservice.model.order.dto.response.OrderResponse;
import com.springbootmicroservices.orderservice.model.order.entity.OrderEntity;
import com.springbootmicroservices.orderservice.model.order.entity.OrderItemEntity;
import com.springbootmicroservices.orderservice.model.order.enums.OrderStatus;
import com.springbootmicroservices.orderservice.model.order.mapper.OrderMapper;
import com.springbootmicroservices.orderservice.repository.OrderRepository;
import com.springbootmicroservices.orderservice.service.OrderService;
import com.springbootmicroservices.orderservice.service.TokenService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final TokenService tokenService;
    private final ProductServiceClient productServiceClient;

    // ... (getCurrentUserId and isCurrentUserAdmin methods are fine, keep them as they are) ...
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String userId = jwt.getClaimAsString(TokenClaims.USER_ID.getValue());
            if (userId == null || userId.isBlank()) {
                log.error("USER_ID claim missing or blank from JWT token for principal: {}", jwt.getSubject());
                throw new IllegalStateException("User ID could not be determined from JWT token.");
            }
            return userId;
        }
        log.warn("Could not extract JWT principal from SecurityContext. Authentication: {}", authentication);
        throw new IllegalStateException("User ID could not be determined from security context (Principal is not JWT or auth is null).");
    }

    private boolean isCurrentUserAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null) {
            return authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(auth -> "ROLE_ADMIN".equals(auth));
        }
        return false;
    }

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest createOrderRequest) {
        String userId = getCurrentUserId();
        log.info("OrderServiceImpl :: createOrder initiated by User ID: {}", userId);

        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setUserId(userId);
        orderEntity.setOrderStatus(OrderStatus.PENDING_PAYMENT);
        orderEntity.setShippingAddressJson(orderMapper.shippingAddressDtoToJson(createOrderRequest.getShippingAddress()));

        BigDecimal calculatedTotalAmount = BigDecimal.ZERO;
        List<OrderItemEntity> newOrderItems = new ArrayList<>();

        for (CartItemDto itemDto : createOrderRequest.getItems()) {
            log.debug("Processing item for order: ProductId={}, Name='{}', Quantity={}",
                    itemDto.getProductId(), itemDto.getName(), itemDto.getQuantity());

            ProductDetailsDto productDetails;
            try {
                // VVVVVV  THIS IS THE CORRECTED CALL VVVVVV
                // It now expects CustomResponse<ProductDetailsDto> from the Feign client
                CustomResponse<ProductDetailsDto> productResponseWrapper = productServiceClient.getProductDetails(itemDto.getProductId());

                if (productResponseWrapper == null || !Boolean.TRUE.equals(productResponseWrapper.getIsSuccess()) || productResponseWrapper.getResponse() == null) {
                    log.error("Failed to fetch product details for Product ID: {}. ProductService response was not successful or body was null.", itemDto.getProductId());
                    throw new RuntimeException("Product details could not be retrieved for: " + itemDto.getName());
                }
                productDetails = productResponseWrapper.getResponse();
                // ^^^^^^ END OF CORRECTION ^^^^^^
            } catch (Exception e) {
                log.error("Error fetching product details for Product ID: {}. Error: {}", itemDto.getProductId(), e.getMessage(), e);
                throw new RuntimeException("Order creation failed: Could not retrieve product details for " + itemDto.getName() + ". Please try again later.", e);
            }

            // ... (rest of the validation and item creation logic is the same and looks good) ...
            if (productDetails.getAmount() == null || productDetails.getAmount() < itemDto.getQuantity()) {
                log.warn("Insufficient stock for Product ID: {}. Name: '{}'. Available: {}, Requested: {}",
                        itemDto.getProductId(), productDetails.getName(), productDetails.getAmount(), itemDto.getQuantity());
                throw new InsufficientStockException("Insufficient stock for product: " + productDetails.getName() +
                        ". Available: " + productDetails.getAmount() + ", Requested: " + itemDto.getQuantity());
            }

            BigDecimal currentPrice = productDetails.getUnitPrice();
            if (currentPrice == null || currentPrice.compareTo(BigDecimal.ZERO) <= 0) {
                log.error("Invalid price (null or non-positive) from ProductService for Product ID: {}", itemDto.getProductId());
                throw new IllegalArgumentException("Invalid price received from product service for: " + productDetails.getName());
            }

            OrderItemEntity orderItem = orderMapper.cartItemDtoToOrderItemEntity(itemDto);
            orderItem.setProductName(productDetails.getName());
            orderItem.setUnitPrice(currentPrice);
            orderItem.setTotalPrice(currentPrice.multiply(BigDecimal.valueOf(itemDto.getQuantity())));
            newOrderItems.add(orderItem);

            calculatedTotalAmount = calculatedTotalAmount.add(orderItem.getTotalPrice());
        }

        orderEntity.setTotalAmount(calculatedTotalAmount);
        newOrderItems.forEach(orderEntity::addItem);

        OrderEntity savedOrder = orderRepository.save(orderEntity);
        log.info("Order entity (ID: {}) created with {} items and total amount: {}. Status: PENDING_PAYMENT",
                savedOrder.getId(), (savedOrder.getItems() != null ? savedOrder.getItems().size() : 0), savedOrder.getTotalAmount());

        for (OrderItemEntity item : savedOrder.getItems()) {
            try {
                log.info("Attempting to reduce stock for Product ID: {}, Quantity: {}", item.getProductId(), item.getQuantity());
                // VVVVVV  THIS IS THE CORRECTED CALL VVVVVV
                // The Feign client method now returns void. If it fails, it will throw an exception.
                productServiceClient.reduceStock(item.getProductId(), new PurchaseQuantityDto(item.getQuantity()));
                // ^^^^^^ END OF CORRECTION ^^^^^^
                log.info("Stock reduction call successful for Product ID: {}", item.getProductId());
            } catch (Exception e) {
                log.error("CRITICAL: Order ID {} created, but failed to reduce stock for Product ID: {}. Error: {}. MANUAL INTERVENTION REQUIRED.",
                        savedOrder.getId(), item.getProductId(), e.getMessage(), e);
                throw new RuntimeException("Order creation partially failed: Could not update product stock for " + item.getProductName() +
                        ". Order ID " + savedOrder.getId() + " might need reconciliation.", e);
            }
        }
        return orderMapper.orderEntityToOrderResponse(savedOrder);
    }

    // ... (rest of the service implementation: getOrderHistoryForCurrentUser, getOrderByIdForCurrentUser, updateOrderStatus, getAllOrdersForAdmin) ...
    // These methods do not need changes based on the error.
    @Override
    @Transactional(readOnly = true)
    public List<OrderHistoryItemResponse> getOrderHistoryForCurrentUser() {
        String userId = getCurrentUserId();
        log.info("OrderServiceImpl :: Fetching order history for User ID: {}", userId);
        List<OrderEntity> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return orderMapper.orderEntitiesToOrderHistoryItemResponses(orders);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByIdForCurrentUser(String orderId) {
        String userId = getCurrentUserId();
        log.info("OrderServiceImpl :: Fetching Order ID: {} for User ID: {}", orderId, userId);

        OrderEntity orderEntity;
        if (isCurrentUserAdmin()) {
            log.debug("User {} is ADMIN, fetching Order ID: {} directly.", userId, orderId);
            orderEntity = orderRepository.findById(orderId)
                    .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));
        } else {
            orderEntity = orderRepository.findByIdAndUserId(orderId, userId)
                    .orElseThrow(() -> {
                        log.warn("OrderServiceImpl :: Order not found or access denied for Order ID: {} and User ID: {}", orderId, userId);
                        return new OrderNotFoundException("Order not found or you do not have permission to view it.");
                    });
        }
        return orderMapper.orderEntityToOrderResponse(orderEntity);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(String orderId, OrderStatus newStatus) {
        log.info("OrderServiceImpl :: Admin (confirmed by @PreAuthorize) updating Order ID: {} to Status: {}", orderId, newStatus);
        OrderEntity orderEntity = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));

        if (orderEntity.getOrderStatus() == newStatus) {
            log.info("Order ID: {} is already in status: {}. No update performed.", orderId, newStatus);
            return orderMapper.orderEntityToOrderResponse(orderEntity);
        }

        log.info("Updating status for Order ID {} from {} to {}", orderId, orderEntity.getOrderStatus(), newStatus);
        orderEntity.setOrderStatus(newStatus);

        OrderEntity updatedOrder = orderRepository.save(orderEntity);
        log.info("OrderServiceImpl :: Order ID: {} status updated successfully to: {}", orderId, newStatus);
        return orderMapper.orderEntityToOrderResponse(updatedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrdersForAdmin(Pageable pageable) {
        log.info("OrderServiceImpl :: Admin fetching all orders with pagination: {}", pageable);
        Page<OrderEntity> orderEntityPage = orderRepository.findAll(pageable);
        return orderEntityPage.map(orderMapper::orderEntityToOrderResponse);
    }
}