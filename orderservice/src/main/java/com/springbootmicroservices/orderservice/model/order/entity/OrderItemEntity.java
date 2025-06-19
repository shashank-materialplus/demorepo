package com.springbootmicroservices.orderservice.model.order.entity;

import com.springbootmicroservices.orderservice.model.common.entity.BaseEntity; // Assuming this path
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@SuperBuilder // Enables builder pattern
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, exclude = {"order"}) // Exclude 'order' to prevent circular issues
@ToString(callSuper = true, exclude = {"order"})      // Exclude 'order'
@Entity
@Table(name = "ORDER_ITEMS")
public class OrderItemEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID")
    private String id;

    // Many OrderItems belong to one Order
    // FetchType.LAZY is generally preferred for performance.
    @NotNull // An order item must belong to an order
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ORDER_ID", nullable = false)
    private OrderEntity order;

    @NotBlank // productId should not be blank
    @Column(name = "PRODUCT_ID", nullable = false)
    private String productId;

    @NotBlank // productName should not be blank
    @Column(name = "PRODUCT_NAME", nullable = false) // Denormalized for easier display in order history
    private String productName;

    @NotNull // quantity should not be null
    @Min(1) // Quantity must be at least 1
    @Column(name = "QUANTITY", nullable = false)
    private Integer quantity;

    @NotNull // unitPrice should not be null
    @Column(name = "UNIT_PRICE", nullable = false, precision = 19, scale = 4) // Price at the time of order
    private BigDecimal unitPrice;

    @NotNull // totalPrice should not be null
    @Column(name = "TOTAL_PRICE", nullable = false, precision = 19, scale = 4) // quantity * unitPrice
    private BigDecimal totalPrice;
}