package com.springbootmicroservices.orderservice.model.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {

    @NotEmpty(message = "Order must contain at least one item")
    @Size(min = 1, message = "Order must contain at least one item")
    @Valid // This will trigger validation for each CartItemDto in the list
    private List<CartItemDto> items;

    @NotNull(message = "Shipping address cannot be null")
    @Valid // This will trigger validation for the ShippingAddressDto
    private ShippingAddressDto shippingAddress;

    // Optional: Client can send a note
    private String orderNotes;
}