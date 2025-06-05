// src/main/java/com/springbootmicroservices/productservice/service/impl/ProductUpdateServiceImpl.java
package com.springbootmicroservices.productservice.service.impl;

import com.springbootmicroservices.productservice.exception.InsufficientStockException; // Import new exception
import com.springbootmicroservices.productservice.exception.ProductAlreadyExistException;
import com.springbootmicroservices.productservice.exception.ProductNotFoundException;
import com.springbootmicroservices.productservice.model.product.Product;
import com.springbootmicroservices.productservice.model.product.dto.request.ProductUpdateRequest;
import com.springbootmicroservices.productservice.model.product.entity.ProductEntity;
import com.springbootmicroservices.productservice.model.product.mapper.ProductEntityToProductMapper;
import com.springbootmicroservices.productservice.model.product.mapper.ProductUpdateRequestToProductEntityMapper;
import com.springbootmicroservices.productservice.repository.ProductRepository;
import com.springbootmicroservices.productservice.service.ProductUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import Transactional



@Service
@RequiredArgsConstructor
public class ProductUpdateServiceImpl implements ProductUpdateService {

    private final ProductRepository productRepository;

    private final ProductUpdateRequestToProductEntityMapper productUpdateRequestToProductEntityMapper =
            ProductUpdateRequestToProductEntityMapper.initialize();

    private final ProductEntityToProductMapper productEntityToProductMapper =
            ProductEntityToProductMapper.initialize();

    @Override
    @Transactional // Good practice for update operations
    public Product updateProductById(String productId, ProductUpdateRequest productUpdateRequest) {

        // Refined uniqueness check: ensure the new name doesn't belong to ANOTHER product
        productRepository.findByNameAndIdNot(productUpdateRequest.getName(), productId).ifPresent(p -> {
            throw new ProductAlreadyExistException("Another product with name = " + productUpdateRequest.getName() + " already exists.");
        });
        // Or if you prefer the simpler check and want to allow renaming to its own current name:
        // if (!productEntityToBeUpdate.getName().equals(productUpdateRequest.getName())) {
        //     checkProductNameUniqueness(productUpdateRequest.getName()); // Only check if name is actually changing
        // }


        final ProductEntity productEntityToBeUpdate = productRepository
                .findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("With given productID = " + productId));

        productUpdateRequestToProductEntityMapper.mapForUpdating(productEntityToBeUpdate, productUpdateRequest);
        ProductEntity updatedProductEntity = productRepository.save(productEntityToBeUpdate);
        return productEntityToProductMapper.map(updatedProductEntity);
    }

    /**
     * Checks if a product with the updated name already exists in the repository.
     * (This version might be problematic if renaming a product to its current name)
     */
    private void checkProductNameUniqueness(final String productName) {
        if (productRepository.existsProductEntityByName(productName)) {
            throw new ProductAlreadyExistException("With given product name = " + productName);
        }
    }


    @Override
    @Transactional // Crucial for atomic stock updates
    public Product processPurchase(final String productId, final int quantityToPurchase) {
        if (quantityToPurchase <= 0) {
            throw new IllegalArgumentException("Quantity to purchase must be positive.");
        }

        ProductEntity productEntity = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));

        if (productEntity.getAmount() == null || productEntity.getAmount().intValue() < quantityToPurchase) {
            throw new InsufficientStockException("Insufficient stock for product: " + productEntity.getName() +
                    ". Available: " + (productEntity.getAmount() != null ? productEntity.getAmount().intValue() : 0) +
                    ", Requested: " + quantityToPurchase);
        }

        // Reduce stock (amount)
        productEntity.setAmount(productEntity.getAmount().subtract(java.math.BigDecimal.valueOf(quantityToPurchase)));
        ProductEntity updatedProductEntity = productRepository.save(productEntity);

        // Here you would typically create an Order record as well

        return productEntityToProductMapper.map(updatedProductEntity);
    }
}