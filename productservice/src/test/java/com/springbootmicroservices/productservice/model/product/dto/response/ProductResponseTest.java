package com.springbootmicroservices.productservice.model.product.dto.response;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ProductResponseTest {

    @Test
    void givenNoArgsConstructor_whenCreateInstance_thenNotNull() {

        // Given
        ProductResponse productResponse = new ProductResponse();

        // Then
        assertThat(productResponse).isNotNull();

    }

    @Test
    void givenAllArgsConstructor_whenCreateInstance_thenFieldsAreSet() {

        // Given
        String id = "123";
        String name = "Product Name";
        BigDecimal amount = new BigDecimal("10.50");
        BigDecimal unitPrice = new BigDecimal("2.25");
        String description = "Product Description";
        String authorName = "Author Name";
        String imageUrl = "https://example.com/image.jpg";
        String category = "Electronics";

        ProductResponse productResponse = new ProductResponse(
                id, name, amount, unitPrice, category, description, authorName, imageUrl
        );

        // Then
        assertThat(productResponse.getId()).isEqualTo(id);
        assertThat(productResponse.getName()).isEqualTo(name);
        assertThat(productResponse.getAmount()).isEqualTo(amount);
        assertThat(productResponse.getUnitPrice()).isEqualTo(unitPrice);
        assertThat(productResponse.getCategory()).isEqualTo(category);
        assertThat(productResponse.getDescription()).isEqualTo(description);
        assertThat(productResponse.getAuthorName()).isEqualTo(authorName);
        assertThat(productResponse.getImageUrl()).isEqualTo(imageUrl);
    }

    @Test
    void givenSetterMethods_whenSetValues_thenGettersReturnCorrectValues() {
        // Given
        ProductResponse productResponse = new ProductResponse();
        String id = "123";
        String name = "Product Name";
        BigDecimal amount = new BigDecimal("10.50");
        BigDecimal unitPrice = new BigDecimal("2.25");
        String description = "Product Description";
        String authorName = "Author Name";
        String imageUrl = "https://example.com/image.jpg";

        // When
        productResponse.setId(id);
        productResponse.setName(name);
        productResponse.setAmount(amount);
        productResponse.setUnitPrice(unitPrice);
        productResponse.setDescription(description);
        productResponse.setAuthorName(authorName);
        productResponse.setImageUrl(imageUrl);

        // Then
        assertThat(productResponse.getId()).isEqualTo(id);
        assertThat(productResponse.getName()).isEqualTo(name);
        assertThat(productResponse.getAmount()).isEqualTo(amount);
        assertThat(productResponse.getUnitPrice()).isEqualTo(unitPrice);
        assertThat(productResponse.getDescription()).isEqualTo(description);
        assertThat(productResponse.getAuthorName()).isEqualTo(authorName);
        assertThat(productResponse.getImageUrl()).isEqualTo(imageUrl);
    }

    @Test
    void givenBuilder_whenBuildObject_thenFieldsAreSet() {
        // Given
        String id = "123";
        String name = "Product Name";
        BigDecimal amount = new BigDecimal("10.50");
        BigDecimal unitPrice = new BigDecimal("2.25");
        String description = "Product Description";
        String authorName = "Author Name";
        String imageUrl = "https://example.com/image.jpg";
        String category = "Electronics"; // New field

        // When
        ProductResponse productResponse = ProductResponse.builder()
                .id(id)
                .name(name)
                .amount(amount)
                .unitPrice(unitPrice)
                .description(description)
                .authorName(authorName)
                .imageUrl(imageUrl)
                .category(category) // New line
                .build();

        // Then
        assertThat(productResponse.getId()).isEqualTo(id);
        assertThat(productResponse.getName()).isEqualTo(name);
        assertThat(productResponse.getAmount()).isEqualTo(amount);
        assertThat(productResponse.getUnitPrice()).isEqualTo(unitPrice);
        assertThat(productResponse.getDescription()).isEqualTo(description);
        assertThat(productResponse.getAuthorName()).isEqualTo(authorName);
        assertThat(productResponse.getImageUrl()).isEqualTo(imageUrl);
        assertThat(productResponse.getCategory()).isEqualTo(category); // New assertion
    }


}