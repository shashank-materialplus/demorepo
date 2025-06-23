package com.springbootmicroservices.productservice.service.impl;

import com.springbootmicroservices.productservice.client.SearchServiceClient;
import com.springbootmicroservices.productservice.client.dto.IndexableProductDto;
import com.springbootmicroservices.productservice.exception.ProductAlreadyExistException;
import com.springbootmicroservices.productservice.model.product.Product;
import com.springbootmicroservices.productservice.model.product.dto.request.ProductCreateRequest;
import com.springbootmicroservices.productservice.model.product.entity.ProductEntity;
import com.springbootmicroservices.productservice.model.product.mapper.ProductCreateRequestToProductEntityMapper;
import com.springbootmicroservices.productservice.model.product.mapper.ProductEntityToProductMapper;
import com.springbootmicroservices.productservice.model.product.mapper.ProductToIndexableProductDtoMapper;
import com.springbootmicroservices.productservice.repository.ProductRepository;
import com.springbootmicroservices.productservice.service.ProductCreateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductCreateServiceImpl implements ProductCreateService {

    private final ProductRepository productRepository;
    private final SearchServiceClient searchServiceClient;

    private final ProductCreateRequestToProductEntityMapper productCreateRequestToProductEntityMapper =
            ProductCreateRequestToProductEntityMapper.initialize();
    private final ProductEntityToProductMapper productEntityToProductMapper =
            ProductEntityToProductMapper.initialize();

    @Override
    public Product createProduct(ProductCreateRequest productCreateRequest) {
        checkUniquenessProductName(productCreateRequest.getName());

        final ProductEntity productEntityToBeSave = productCreateRequestToProductEntityMapper.mapForSaving(productCreateRequest);
        ProductEntity savedProductEntity = productRepository.save(productEntityToBeSave);
        final Product createdProduct = productEntityToProductMapper.map(savedProductEntity);

        // --- NEW: Push to search index after successful creation ---
        try {
            log.info("Pushing new product to search index. Product ID: {}", createdProduct.getId());
            IndexableProductDto dto = ProductToIndexableProductDtoMapper.INSTANCE.productToIndexableProductDto(createdProduct);
            searchServiceClient.indexProduct(dto);
            log.info("Successfully pushed product {} to search index.", createdProduct.getId());
        } catch (Exception e) {
            log.error("CRITICAL: Failed to index new product {}. Search index is now out of sync.", createdProduct.getId(), e);
        }

        return createdProduct;
    }

    private void checkUniquenessProductName(final String productName) {
        if (productRepository.existsProductEntityByName(productName)) {
            throw new ProductAlreadyExistException("There is another product with given name: " + productName);
        }
    }
}