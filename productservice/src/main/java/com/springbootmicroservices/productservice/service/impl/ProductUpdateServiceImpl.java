package com.springbootmicroservices.productservice.service.impl;

import com.springbootmicroservices.productservice.client.SearchServiceClient;
import com.springbootmicroservices.productservice.client.dto.IndexableProductDto;
import com.springbootmicroservices.productservice.exception.InsufficientStockException;
import com.springbootmicroservices.productservice.exception.ProductAlreadyExistException;
import com.springbootmicroservices.productservice.exception.ProductNotFoundException;
import com.springbootmicroservices.productservice.model.product.Product;
import com.springbootmicroservices.productservice.model.product.dto.request.ProductUpdateRequest;
import com.springbootmicroservices.productservice.model.product.entity.ProductEntity;
import com.springbootmicroservices.productservice.model.product.mapper.ProductEntityToProductMapper;
import com.springbootmicroservices.productservice.model.product.mapper.ProductToIndexableProductDtoMapper;
import com.springbootmicroservices.productservice.model.product.mapper.ProductUpdateRequestToProductEntityMapper;
import com.springbootmicroservices.productservice.repository.ProductRepository;
import com.springbootmicroservices.productservice.service.ProductUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductUpdateServiceImpl implements ProductUpdateService {

    private final ProductRepository productRepository;
    private final SearchServiceClient searchServiceClient;

    private final ProductUpdateRequestToProductEntityMapper productUpdateRequestToProductEntityMapper =
            ProductUpdateRequestToProductEntityMapper.initialize();
    private final ProductEntityToProductMapper productEntityToProductMapper =
            ProductEntityToProductMapper.initialize();

    @Override
    @Transactional
    public Product updateProductById(String productId, ProductUpdateRequest productUpdateRequest) {
        productRepository.findByNameAndIdNot(productUpdateRequest.getName(), productId).ifPresent(p -> {
            throw new ProductAlreadyExistException("Another product with name = " + productUpdateRequest.getName() + " already exists.");
        });

        final ProductEntity productEntityToBeUpdate = productRepository
                .findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("With given productID = " + productId));

        productUpdateRequestToProductEntityMapper.mapForUpdating(productEntityToBeUpdate, productUpdateRequest);
        ProductEntity updatedProductEntity = productRepository.save(productEntityToBeUpdate);
        final Product updatedProduct = productEntityToProductMapper.map(updatedProductEntity);

        // --- NEW: Push updated product to search index ---
        indexProductInSearchService(updatedProduct);

        return updatedProduct;
    }

    @Override
    @Transactional
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

        productEntity.setAmount(productEntity.getAmount().subtract(java.math.BigDecimal.valueOf(quantityToPurchase)));
        ProductEntity updatedProductEntity = productRepository.save(productEntity);
        final Product updatedProduct = productEntityToProductMapper.map(updatedProductEntity);

        // --- NEW: Update search index with new stock amount ---
        indexProductInSearchService(updatedProduct);

        return updatedProduct;
    }

    // Helper method to keep the indexing logic DRY (Don't Repeat Yourself)
    private void indexProductInSearchService(Product product) {
        try {
            log.info("Pushing updated product to search index. Product ID: {}", product.getId());
            IndexableProductDto dto = ProductToIndexableProductDtoMapper.INSTANCE.productToIndexableProductDto(product);
            searchServiceClient.indexProduct(dto);
            log.info("Successfully pushed updated product {} to search index.", product.getId());
        } catch (Exception e) {
            log.error("CRITICAL: Failed to index updated product {}. Search index is now out of sync.", product.getId(), e);
        }
    }
}