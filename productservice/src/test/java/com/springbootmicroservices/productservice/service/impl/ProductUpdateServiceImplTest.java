package com.springbootmicroservices.productservice.service.impl;

import com.springbootmicroservices.productservice.base.AbstractBaseServiceTest;
import com.springbootmicroservices.productservice.exception.ProductAlreadyExistException;
import com.springbootmicroservices.productservice.exception.ProductNotFoundException;
import com.springbootmicroservices.productservice.model.product.Product;
import com.springbootmicroservices.productservice.model.product.dto.request.ProductUpdateRequest;
import com.springbootmicroservices.productservice.model.product.entity.ProductEntity;
import com.springbootmicroservices.productservice.model.product.mapper.ProductEntityToProductMapper;
import com.springbootmicroservices.productservice.model.product.mapper.ProductUpdateRequestToProductEntityMapper;
import com.springbootmicroservices.productservice.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ProductUpdateServiceImplTest extends AbstractBaseServiceTest {

    @InjectMocks
    private ProductUpdateServiceImpl productUpdateService;

    @Mock
    private ProductRepository productRepository;

    private final ProductUpdateRequestToProductEntityMapper productUpdateRequestToProductEntityMapper = ProductUpdateRequestToProductEntityMapper.initialize();
    private final ProductEntityToProductMapper productEntityToProductMapper = ProductEntityToProductMapper.initialize();

    @Test
    void updateProductById_WhenUpdateIsValid_ShouldReturnUpdatedProduct() {
        // Given
        String productId = "1";
        String newProductName = "New Product Name";
        ProductUpdateRequest request = ProductUpdateRequest.builder()
                .name(newProductName).unitPrice(BigDecimal.TEN).amount(BigDecimal.ONE).build();
        ProductEntity existingProduct = ProductEntity.builder().id(productId).name("Old Name").build();

        // When: Mock the new service logic
        when(productRepository.findByNameAndIdNot(newProductName, productId)).thenReturn(Optional.empty()); // Name is available
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(ProductEntity.class))).thenReturn(existingProduct);

        // Then
        Product updatedProduct = productUpdateService.updateProductById(productId, request);

        assertNotNull(updatedProduct);
        assertEquals(newProductName, updatedProduct.getName());

        // Verify the new service logic calls
        verify(productRepository).findByNameAndIdNot(newProductName, productId);
        verify(productRepository).findById(productId);
        verify(productRepository).save(any(ProductEntity.class));
    }

    @Test
    void updateProductById_WhenProductNotFound_ShouldThrowException() {
        // Given
        String productId = "1";
        ProductUpdateRequest request = ProductUpdateRequest.builder().name("any name").build();

        // Mock the uniqueness check, but the findById will fail first
        when(productRepository.findByNameAndIdNot(anyString(), anyString())).thenReturn(Optional.empty());
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductNotFoundException.class, () -> productUpdateService.updateProductById(productId, request));

        // Verify findById was called, but save was not
        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(any(ProductEntity.class));
    }

    @Test
    void updateProductById_WhenNewNameAlreadyExists_ShouldThrowException() {
        // Given
        String productId = "1";
        String existingProductName = "Existing Product";
        ProductUpdateRequest request = ProductUpdateRequest.builder().name(existingProductName).build();

        // Mock that another product (id="2") already has this name
        when(productRepository.findByNameAndIdNot(existingProductName, productId))
                .thenReturn(Optional.of(ProductEntity.builder().id("2").build()));

        // When & Then
        assertThrows(ProductAlreadyExistException.class, () -> productUpdateService.updateProductById(productId, request));

        // Verify that the process stopped after the uniqueness check
        verify(productRepository).findByNameAndIdNot(existingProductName, productId);
        verify(productRepository, never()).findById(anyString());
        verify(productRepository, never()).save(any(ProductEntity.class));
    }
}