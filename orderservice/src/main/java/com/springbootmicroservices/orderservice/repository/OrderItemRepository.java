package com.springbootmicroservices.orderservice.repository;

import com.springbootmicroservices.orderservice.model.order.entity.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// import java.util.List; // Uncomment if specific queries are needed

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItemEntity, String> {
    // Custom query methods can be added if needed, for example:
    // List<OrderItemEntity> findByOrderId(String orderId);
    // List<OrderItemEntity> findByProductId(String productId);

    // However, often OrderItems are accessed via the OrderEntity's collection of items,
    // especially if CascadeType.ALL and orphanRemoval=true are used.
}