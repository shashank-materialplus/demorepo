package com.springbootmicroservices.productservice.service.impl;

import com.springbootmicroservices.productservice.base.AbstractBaseServiceTest; // Assuming this sets up Mockito or similar
import com.springbootmicroservices.productservice.exception.ProductNotFoundException;
import com.springbootmicroservices.productservice.model.product.entity.ProductEntity;
import com.springbootmicroservices.productservice.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith; // Add if AbstractBaseServiceTest doesn't handle it
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension; // Add if AbstractBaseServiceTest doesn't handle it
import org.springframework.data.repository.CrudRepository; // <<<--- ADD THIS IMPORT

import java.util.Optional;
import java.util.UUID; // For generating unique IDs if needed, though "1" is fine for test

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

// If AbstractBaseServiceTest doesn't use @ExtendWith(MockitoExtension.class), add it here
@ExtendWith(MockitoExtension.class)
class ProductDeleteServiceImplTest extends AbstractBaseServiceTest {

    @InjectMocks
    private ProductDeleteServiceImpl productDeleteService;

    @Mock
    private ProductRepository productRepository; // This repository is the one with ambiguous delete


    @Test
    void givenProductId_whenDeleteProduct_thenReturnProductDeleted() {

        // Given
        String productId = "test-id-1"; // Use a more descriptive ID or UUID
        ProductEntity existingProductEntity = new ProductEntity();
        existingProductEntity.setId(productId);
        // existingProductEntity.setName("Test Product"); // Optional: add more fields if relevant

        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProductEntity));

        // For void methods, doNothing().when(...) is often used if you need to stub it,
        // but for verification, it's not always necessary to stub it.
        // The key is the verify step.
        // If JpaSpecificationExecutor is indeed part of ProductRepository, then:
        // doNothing().when((CrudRepository<ProductEntity, String>) productRepository).delete(any(ProductEntity.class)); // Stub specific delete

        // When
        productDeleteService.deleteProductById(productId);

        // Then
        verify(productRepository, times(1)).findById(productId);

        // Apply the fix: Cast to CrudRepository to specify which delete method to verify
        verify((CrudRepository<ProductEntity, String>) productRepository, times(1)).delete(existingProductEntity);
    }

    @Test
    void givenProductId_whenProductNotFound_thenThrowProductNotFoundException() {

        // Given
        String productId = "test-id-2";
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(ProductNotFoundException.class, () -> productDeleteService.deleteProductById(productId));

        // Verify
        verify(productRepository, times(1)).findById(productId);
        // Verifying delete was *never* called is correct here
        verify(productRepository, never()).delete(any(ProductEntity.class));
        // For the `never()` case with an ambiguous method, `any()` is usually fine because if *any* delete
        // matching that argument type was called, it would fail.
        // If it still complained, you could do:
        // verify((CrudRepository<ProductEntity, String>) productRepository, never()).delete(any(ProductEntity.class));
    }
}