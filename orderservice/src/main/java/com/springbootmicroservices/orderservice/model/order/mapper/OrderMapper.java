package com.springbootmicroservices.orderservice.model.order.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
// BaseMapper is not strictly needed here if you don't use its generic map methods directly in this interface's signature
// import com.springbootmicroservices.orderservice.model.common.mapper.BaseMapper;
import com.springbootmicroservices.orderservice.model.order.dto.request.CartItemDto;
import com.springbootmicroservices.orderservice.model.order.dto.request.ShippingAddressDto; // Correct import
import com.springbootmicroservices.orderservice.model.order.dto.response.OrderHistoryItemResponse;
import com.springbootmicroservices.orderservice.model.order.dto.response.OrderItemResponse;
import com.springbootmicroservices.orderservice.model.order.dto.response.OrderResponse;
import com.springbootmicroservices.orderservice.model.order.entity.OrderEntity;
import com.springbootmicroservices.orderservice.model.order.entity.OrderItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings; // This was correct
import org.mapstruct.Named;
// import org.mapstruct.factory.Mappers; // Not needed with componentModel="spring"
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired; // For injecting ObjectMapper

import java.io.IOException;
import java.util.List;
import java.util.Collections; // For returning empty list

// Use "spring" to allow DI of this mapper and potentially ObjectMapper
// Add uses = {ObjectMapper.class} if you want MapStruct to potentially use a Spring-managed ObjectMapper
// for some conversions, though explicit injection in an abstract class is often clearer for custom logic.
@Mapper(componentModel = "spring")
// public interface OrderMapper extends BaseMapper<Object, Object> { // Extending BaseMapper<Object,Object> is not useful
public interface OrderMapper {

    // Logger for errors - better to inject if this becomes an abstract class, or keep static if interface
    // For an interface with default methods, a static final logger is more appropriate.
    Logger log = LoggerFactory.getLogger(OrderMapper.class);

    // ObjectMapper instance - prefer injection if this becomes an abstract class
    // For default methods in an interface, you'd typically instantiate it or pass it.
    // If Spring's ObjectMapper bean is to be used, this needs to be an abstract class with @Autowired.
    // For now, we'll keep your local instantiation in the default methods, but this is an area for refinement.
    // ObjectMapper getObjectMapper(); // This abstract method is if MapStruct were to provide the impl


    // --- OrderItem Mappings ---
    @Mappings({
            @Mapping(target = "id", ignore = true), // ID will be generated by DB
            @Mapping(target = "order", ignore = true), // Will be set manually via OrderEntity.addItem()
            @Mapping(target = "totalPrice", ignore = true), // Calculated in service before saving
            @Mapping(target = "createdAt", ignore = true), // From BaseEntity
            @Mapping(target = "createdBy", ignore = true), // From BaseEntity
            @Mapping(target = "updatedAt", ignore = true), // From BaseEntity
            @Mapping(target = "updatedBy", ignore = true), // From BaseEntity
            @Mapping(source = "name", target = "productName") // Map CartItemDto.name to OrderItemEntity.productName
            // productId, unitPrice, quantity should map automatically by name if present in CartItemDto
    })
    OrderItemEntity cartItemDtoToOrderItemEntity(CartItemDto cartItemDto);

    // MapStruct generates list mappings automatically if the single item mapping exists
    List<OrderItemEntity> cartItemDtosToOrderItemEntities(List<CartItemDto> cartItemDtos);

    // For mapping OrderItemEntity to OrderItemResponse
    // Fields like productId, productName, quantity, unitPrice, totalPrice should map by name
    OrderItemResponse orderItemEntityToOrderItemResponse(OrderItemEntity orderItemEntity);

    List<OrderItemResponse> orderItemEntitiesToOrderItemResponses(List<OrderItemEntity> orderItemEntities);


    // --- Order Mappings ---
    // Mapping from OrderEntity to OrderResponse
    @Mappings({
            @Mapping(source = "shippingAddressJson", target = "shippingAddress", qualifiedByName = "jsonToShippingAddressDto"),
            @Mapping(source = "items", target = "items") // MapStruct handles List<OrderItemEntity> to List<OrderItemResponse>
            // Implicitly maps: id, userId, totalAmount, orderStatus, externalPaymentId,
            // stripePaymentIntentId, stripeClientSecret, createdAt, updatedAt
            // @Mapping(source = "orderNotes", target = "orderNotes") // Uncomment if OrderEntity has orderNotes
    })
    @Mapping(target = "orderNotes", ignore = true)
    OrderResponse orderEntityToOrderResponse(OrderEntity orderEntity);

    // Mapping from OrderEntity to OrderHistoryItemResponse
    @Mappings({
            @Mapping(source = "createdAt", target = "orderDate"),
            // Robust expression for itemCount, handles null items list and null quantity in item
            @Mapping(target = "itemCount", expression = "java(orderEntity.getItems() == null ? 0 : orderEntity.getItems().stream().mapToInt(item -> item.getQuantity() == null ? 0 : item.getQuantity()).sum())"),
            @Mapping(source = "orderStatus", target = "status")
            // Implicitly maps: id, totalAmount
    })
    OrderHistoryItemResponse orderEntityToOrderHistoryItemResponse(OrderEntity orderEntity);

    List<OrderHistoryItemResponse> orderEntitiesToOrderHistoryItemResponses(List<OrderEntity> orderEntities);


    // --- Helper methods for JSON conversion of ShippingAddress ---
    // These methods are fine with local ObjectMapper instantiation for simplicity.
    // For more complex global Jackson configurations, injecting the Spring-managed ObjectMapper
    // into an abstract class version of this mapper would be preferred.

    @Named("shippingAddressDtoToJson")
    default String shippingAddressDtoToJson(ShippingAddressDto shippingAddressDto) {
        if (shippingAddressDto == null) {
            return null;
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule()); // Good for any date/time types in ShippingAddressDto
            return objectMapper.writeValueAsString(shippingAddressDto);
        } catch (JsonProcessingException e) {
            log.error("Error converting ShippingAddressDto to JSON string: {}. Details: {}", shippingAddressDto.toString(), e.getMessage());
            // Consider throwing a more specific custom mapping exception or re-throwing a RuntimeException
            throw new RuntimeException("Mapping error: Could not convert ShippingAddressDto to JSON", e);
        }
    }

    @Named("jsonToShippingAddressDto")
    default ShippingAddressDto jsonToShippingAddressDto(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            return objectMapper.readValue(json, ShippingAddressDto.class);
        } catch (IOException e) { // Catch IOException for readValue specifically
            log.error("Error converting JSON string to ShippingAddressDto. JSON: [{}]. Details: {}",
                    (json.length() > 100 ? json.substring(0, 100) + "..." : json), // Log snippet of JSON
                    e.getMessage());
            throw new RuntimeException("Mapping error: Could not convert JSON to ShippingAddressDto", e);
        }
    }
}