package com.springbootmicroservices.productservice.service.impl;

import com.springbootmicroservices.productservice.base.AbstractBaseServiceTest;
import com.springbootmicroservices.productservice.exception.ProductNotFoundException;
import com.springbootmicroservices.productservice.model.product.entity.ProductEntity;
import com.springbootmicroservices.productservice.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductDeleteServiceImplTest extends AbstractBaseServiceTest {

    @InjectMocks
    private ProductDeleteServiceImpl productDeleteService;

    @Mock
    private ProductRepository productRepository;

    @Test
    void deleteProductById_WhenProductExists_ShouldDeleteSuccessfully() {
        // Given
        String productId = "test-id-1";
        ProductEntity productToDelete = ProductEntity.builder().id(productId).build();

        // When: Mock the findById call to return our entity
        when(productRepository.findById(productId)).thenReturn(Optional.of(productToDelete));
        // We don't mock the void delete method, we verify it below.

        // Then
        productDeleteService.deleteProductById(productId);

        // Verify the sequence of calls in the service method
        verify(productRepository, times(1)).findById(productId);
        // THIS IS THE FIX: Verify that the delete method was called with the entity we found
        verify(productRepository, times(1)).delete(productToDelete);
    }

    @Test
    void deleteProductById_WhenProductNotFound_ShouldThrowException() {
        // Given
        String productId = "non-existent-id";
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductNotFoundException.class, () -> productDeleteService.deleteProductById(productId));

        // Verify delete was never called
        verify(productRepository, never()).delete(any(ProductEntity.class));
    }
}