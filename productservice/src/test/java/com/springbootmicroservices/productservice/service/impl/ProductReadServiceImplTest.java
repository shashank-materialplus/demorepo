package com.springbootmicroservices.productservice.service.impl;

import com.springbootmicroservices.productservice.base.AbstractBaseServiceTest;
import com.springbootmicroservices.productservice.exception.ProductNotFoundException;
import com.springbootmicroservices.productservice.model.common.CustomPage;
import com.springbootmicroservices.productservice.model.common.CustomPaging;
import com.springbootmicroservices.productservice.model.product.Product;
import com.springbootmicroservices.productservice.model.product.dto.request.ProductPagingRequest;
import com.springbootmicroservices.productservice.model.product.entity.ProductEntity;
import com.springbootmicroservices.productservice.model.product.mapper.ListProductEntityToListProductMapper;
import com.springbootmicroservices.productservice.model.product.mapper.ProductEntityToProductMapper;
import com.springbootmicroservices.productservice.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductReadServiceImplTest extends AbstractBaseServiceTest {

    @InjectMocks
    private ProductReadServiceImpl productReadService;

    @Mock
    private ProductRepository productRepository;

    private final ProductEntityToProductMapper productEntityToProductMapper = ProductEntityToProductMapper.initialize();
    private final ListProductEntityToListProductMapper listProductEntityToListProductMapper = ListProductEntityToListProductMapper.initialize();

    @Test
    void getProductById_WhenFound_ShouldReturnProduct() {
        // Given
        String productId = "1";
        ProductEntity productEntity = new ProductEntity();
        productEntity.setId(productId);
        when(productRepository.findById(productId)).thenReturn(Optional.of(productEntity));

        // When
        Product result = productReadService.getProductById(productId);

        // Then
        assertNotNull(result);
        assertEquals(productId, result.getId());
        verify(productRepository).findById(productId);
    }

    @Test
    void getProductById_WhenNotFound_ShouldThrowException() {
        // Given
        String productId = "1";
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductNotFoundException.class, () -> productReadService.getProductById(productId));
        verify(productRepository).findById(productId);
    }

    @Test
    void getProducts_WhenProductsExist_ShouldReturnCustomPage() {
        // Given
        ProductPagingRequest pagingRequest = ProductPagingRequest.builder()
                .pagination(CustomPaging.builder().pageSize(1).pageNumber(1).build())
                .build();
        Page<ProductEntity> productEntityPage = new PageImpl<>(Collections.singletonList(new ProductEntity()));

        // When: THIS IS THE FIX - Mock the correct findAll method
        when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(productEntityPage);

        // Then
        CustomPage<Product> result = productReadService.getProducts(pagingRequest);

        assertNotNull(result);
        assertFalse(result.getContent().isEmpty());
    }

    @Test
    void getProducts_WhenNoProductsExist_ShouldThrowException() {
        // Given
        ProductPagingRequest pagingRequest = ProductPagingRequest.builder()
                .pagination(CustomPaging.builder().pageSize(1).pageNumber(1).build())
                .build();
        Page<ProductEntity> emptyPage = Page.empty();

        // When: THIS IS THE FIX - Mock the correct findAll method
        when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(emptyPage);

        // Then
        assertThrows(ProductNotFoundException.class, () -> productReadService.getProducts(pagingRequest));
    }
}