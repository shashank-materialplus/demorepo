package com.springbootmicroservices.productservice.service.impl;

import com.springbootmicroservices.productservice.exception.ProductNotFoundException;
import com.springbootmicroservices.productservice.model.common.CustomPage;
import com.springbootmicroservices.productservice.model.product.Product;
import com.springbootmicroservices.productservice.model.product.dto.request.ProductPagingRequest;
import com.springbootmicroservices.productservice.model.product.entity.ProductEntity;
import com.springbootmicroservices.productservice.model.product.mapper.ListProductEntityToListProductMapper;
import com.springbootmicroservices.productservice.model.product.mapper.ProductEntityToProductMapper;
import com.springbootmicroservices.productservice.repository.ProductRepository;
import com.springbootmicroservices.productservice.service.ProductReadService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification; // For specifications
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;


import java.util.List;

/**
 * Service implementation named {@link ProductReadServiceImpl} for reading products.
 */
@Service
@RequiredArgsConstructor
public class ProductReadServiceImpl implements ProductReadService {


    private final ProductRepository productRepository;

    private final ProductEntityToProductMapper productEntityToProductMapper = ProductEntityToProductMapper.initialize();

    private final ListProductEntityToListProductMapper listProductEntityToListProductMapper =
            ListProductEntityToListProductMapper.initialize();

    /**
     * Retrieves a product by its unique ID.
     *
     * @param productId The ID of the product to retrieve.
     * @return The Product object corresponding to the given ID.
     * @throws ProductNotFoundException If no product with the given ID exists.
     */
    @Override
    public Product getProductById(String productId) {

        final ProductEntity productEntityFromDB = productRepository
                .findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("With given productID = " + productId));

        return productEntityToProductMapper.map(productEntityFromDB);
    }


    /**
     * Retrieves a page of products based on the paging request criteria.
     *
     * @param productPagingRequest The paging request criteria.
     * @return A CustomPage containing the list of products that match the paging criteria.
     * @throws ProductNotFoundException If no products are found based on the paging criteria.
     */


    @Override
    public CustomPage<Product> getProducts(ProductPagingRequest productPagingRequest) {

        // Handle Sorting
        Sort sort = Sort.unsorted();
        if (productPagingRequest.getSortBy() != null && !productPagingRequest.getSortBy().isEmpty()) {
            try {
                String[] sortParams = productPagingRequest.getSortBy().split(",");
                String property = sortParams[0];
                Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc") ?
                        Sort.Direction.DESC : Sort.Direction.ASC;
                sort = Sort.by(direction, property);
            } catch (Exception e) {
                // Log error or handle invalid sort parameter
                System.err.println("Invalid sort parameter: " + productPagingRequest.getSortBy());
            }
        }

        Pageable pageable = PageRequest.of(
                productPagingRequest.getPagination().getPageNumberForPageable(), // Use 0-based
                productPagingRequest.getPagination().getPageSize(),
                sort
        );

        // Handle Specifications for filtering (e.g., maxPrice)
        Specification<ProductEntity> spec = Specification.where(null); // Start with a non-null spec

        if (productPagingRequest.getMaxPrice() != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get("unitPrice"), productPagingRequest.getMaxPrice())
            );
        }

        // Add other filters to spec if any...

        final Page<ProductEntity> productEntityPage = productRepository.findAll(spec, pageable);

        if (productEntityPage.getContent().isEmpty()) {
            // Consider if throwing an exception for no results is always desired.
            // Sometimes returning an empty page is acceptable.
            // For now, keeping original logic:
            throw new ProductNotFoundException("Couldn't find any Product matching criteria");
        }

        final List<Product> productDomainModels = listProductEntityToListProductMapper
                .toProductList(productEntityPage.getContent());

        return CustomPage.of(productDomainModels, productEntityPage);
    }

    @Override
    public List<Product> getProductsByCategory(String category) {

        final List<ProductEntity> productEntities = productRepository.findByCategory(category);

        if (productEntities.isEmpty()) {
            throw new ProductNotFoundException("No products found for category: " + category);
        }

        return listProductEntityToListProductMapper.toProductList(productEntities);
    }

}
