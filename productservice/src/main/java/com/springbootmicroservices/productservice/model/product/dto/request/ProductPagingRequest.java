// com.springbootmicroservices.productservice.model.product.dto.request.ProductPagingRequest
package com.springbootmicroservices.productservice.model.product.dto.request;

import com.springbootmicroservices.productservice.model.common.dto.request.CustomPagingRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ProductPagingRequest extends CustomPagingRequest {
    private String sortBy; // e.g., "name,asc", "unitPrice,desc"
    private BigDecimal maxPrice;
    // You could add other filters like minPrice, nameContains, etc.
}