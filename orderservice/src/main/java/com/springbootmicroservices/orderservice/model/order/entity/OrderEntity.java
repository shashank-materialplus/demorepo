package com.springbootmicroservices.orderservice.model.order.entity;

import com.springbootmicroservices.orderservice.model.common.entity.BaseEntity;
import com.springbootmicroservices.orderservice.model.order.enums.OrderStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList; // Import ArrayList
import java.util.List;

@Getter
@Setter
@SuperBuilder // Enables builder pattern for this entity and its superclass fields
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, exclude = {"items"}) // Exclude collections from equals/hashCode
@ToString(callSuper = true, exclude = {"items"})      // Exclude collections from toString
@Entity
@Table(name = "ORDERS") // Using "ORDERS" as "ORDER" is often a reserved SQL keyword
public class OrderEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID")
    private String id;

    @NotBlank // Ensure userId is not blank
    @Column(name = "USER_ID", nullable = false, updatable = false) // User ID should not be updatable
    private String userId;

    // orphanRemoval = true: if an OrderItemEntity is removed from this list, it will be deleted from the DB.
    // cascade = CascadeType.ALL: all operations (persist, merge, remove, refresh, detach) on OrderEntity
    // will be cascaded to its OrderItemEntities.
    @Builder.Default // Initialize with an empty list when using Lombok's @Builder
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<OrderItemEntity> items = new ArrayList<>(); // Initialize to avoid NullPointerExceptions

    @NotNull // totalAmount should not be null
    @Column(name = "TOTAL_AMOUNT", nullable = false, precision = 19, scale = 4) // Increased precision
    private BigDecimal totalAmount;

    @NotNull // orderStatus should not be null
    @Enumerated(EnumType.STRING)
    @Column(name = "ORDER_STATUS", nullable = false, length = 50) // Added length for DB
    private OrderStatus orderStatus;

    // Store shipping address as a JSON string or consider an @Embedded object or separate @OneToOne entity
    @Lob // For potentially larger string data if storing as JSON
    @Column(name = "SHIPPING_ADDRESS_JSON", columnDefinition = "TEXT") // Use TEXT for longer JSON
    private String shippingAddressJson;

    @Column(name = "PAYMENT_ID_EXTERNAL", length = 100) // From Stripe or other payment gateway
    private String externalPaymentId;

    @Column(name = "STRIPE_PAYMENT_INTENT_ID", length = 100)
    private String stripePaymentIntentId;

    @Column(name = "STRIPE_CLIENT_SECRET", length = 255) // Client secrets can be longer
    private String stripeClientSecret;

    // Helper method to add items and set the bidirectional relationship
    public void addItem(OrderItemEntity item) {
        if (items == null) {
            items = new ArrayList<>();
        }
        items.add(item);
        item.setOrder(this);
    }

    public void removeItem(OrderItemEntity item) {
        if (items != null) {
            items.remove(item);
            item.setOrder(null);
        }
    }
}