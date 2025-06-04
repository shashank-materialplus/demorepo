package com.springbootmicroservices.productservice.model.product.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.*;
import jakarta.validation.constraints.NotBlank;


import java.math.BigDecimal;

/**
 * Represents a request object for creating a new product as {@link ProductCreateRequest}.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCreateRequest {

    @Size(
            min = 1,
            message = "Product name can't be blank."
    )
    private String name;

    @DecimalMin(
            value = "0.0001",
            message = "Amount must be bigger than 0"
    )
    private BigDecimal amount;

    @DecimalMin(
            value = "0.0001",
            message = "Unit Price must be bigger than 0"
    )
    private BigDecimal unitPrice;

    @NotBlank(message = "Category cannot be blank.")
    private String category;

    @Size(max = 1000, message = "Description can't exceed 1000 characters")
    private String description;

    @NotBlank(message = "Author name is required")
    private String authorName;

    @NotBlank(message = "Image URL is required")
    private String imageUrl;

}
