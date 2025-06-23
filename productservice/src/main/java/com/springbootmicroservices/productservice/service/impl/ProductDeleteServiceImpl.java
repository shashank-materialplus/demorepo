package com.springbootmicroservices.productservice.service.impl;

import com.springbootmicroservices.productservice.client.SearchServiceClient;
import com.springbootmicroservices.productservice.exception.ProductNotFoundException;
import com.springbootmicroservices.productservice.model.product.entity.ProductEntity;
import com.springbootmicroservices.productservice.repository.ProductRepository;
import com.springbootmicroservices.productservice.service.ProductDeleteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductDeleteServiceImpl implements ProductDeleteService {

    private final ProductRepository productRepository;
    private final SearchServiceClient searchServiceClient;

    @Override
    @Transactional
    public void deleteProductById(String productId) {
        // Find the entity first to ensure it exists before we do anything.
        ProductEntity productEntityToBeDelete = productRepository
                .findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("With given productID = " + productId));

        // Delete from our local database
        productRepository.delete(productEntityToBeDelete);
        log.info("Successfully deleted product {} from the database.", productId);

        // --- NEW: Now, delete from the search index ---
        try {
            log.info("Deleting product from search index. Product ID: {}", productId);
            searchServiceClient.deleteProduct(productId);
            log.info("Successfully sent delete request for product {} to search index.", productId);
        } catch (Exception e) {
            // If this fails, the DB has already been updated. Log a critical error for reconciliation.
            log.error("CRITICAL: Product {} was deleted from DB, but failed to be deleted from search index. A ghost document may exist.", productId, e);
        }
    }
}