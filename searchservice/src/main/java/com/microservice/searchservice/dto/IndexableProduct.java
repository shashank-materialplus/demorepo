package com.microservice.searchservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.Builder;
import java.util.Map;
import java.time.Instant;
import java.time.LocalDateTime;

@Data
@Builder
public class IndexableProduct {
    @NotBlank(message = "Document ID cannot be null or empty.")
    private String documentId;

    @NotBlank(message = "Title cannot be null or empty.")
    private String title;

    private String description; // Description can be optional

    @NotNull(message = "Price cannot be null.")
    @PositiveOrZero(message = "Price must be a non-negative value.")
    private Double price;

    private String imageUrl;

    @NotBlank(message = "Category cannot be null or empty.")
    private String category;

    private String author;

    @NotNull(message = "Stock cannot be null.")
    @PositiveOrZero(message = "Stock must be a non-negative value.")
    private Integer stock;

    private LocalDateTime createdAt;
    private boolean inStock;
    private Map<String, Object> metadata;
}