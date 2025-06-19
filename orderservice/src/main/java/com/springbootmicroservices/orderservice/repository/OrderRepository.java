package com.springbootmicroservices.orderservice.repository;

import com.springbootmicroservices.orderservice.model.order.entity.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, String>, JpaSpecificationExecutor<OrderEntity> {

    // Find all orders for a specific user, ordered by creation date descending
    List<OrderEntity> findByUserIdOrderByCreatedAtDesc(String userId);

    // For paginated order history
    Page<OrderEntity> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    // Find a specific order for a specific user (to ensure user can only access their own orders)
    Optional<OrderEntity> findByIdAndUserId(String orderId, String userId);

    // Optional: Find by paymentIntentId if needed for webhook processing or payment status checks
    Optional<OrderEntity> findByStripePaymentIntentId(String paymentIntentId);
}