package com.springbootmicroservices.productservice.controller;

import com.springbootmicroservices.productservice.model.common.CustomPage;
import com.springbootmicroservices.productservice.model.common.CustomPaging;
import com.springbootmicroservices.productservice.model.common.dto.response.CustomPagingResponse;
import com.springbootmicroservices.productservice.model.common.dto.response.CustomResponse;
import com.springbootmicroservices.productservice.model.product.Product;
import com.springbootmicroservices.productservice.model.product.dto.request.ProductCreateRequest;
import com.springbootmicroservices.productservice.model.product.dto.request.ProductPagingRequest;
import com.springbootmicroservices.productservice.model.product.dto.request.ProductUpdateRequest;
import com.springbootmicroservices.productservice.model.product.dto.response.ProductResponse;
import com.springbootmicroservices.productservice.model.product.mapper.CustomPageToCustomPagingResponseMapper;
import com.springbootmicroservices.productservice.model.product.mapper.ProductToProductResponseMapper;
import com.springbootmicroservices.productservice.service.ProductCreateService;
import com.springbootmicroservices.productservice.service.ProductDeleteService;
import com.springbootmicroservices.productservice.service.ProductReadService;
import com.springbootmicroservices.productservice.service.ProductUpdateService;
import com.springbootmicroservices.productservice.model.product.dto.request.PurchaseRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.math.BigDecimal;

/**
 * REST controller named {@link ProductController} for managing products.
 * Provides endpoints to create, read, update, and delete products.
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Validated
public class ProductController {

    private final ProductCreateService productCreateService;
    private final ProductReadService productReadService;
    private final ProductUpdateService productUpdateService;
    private final ProductDeleteService productDeleteService;

    private final ProductToProductResponseMapper productToProductResponseMapper = ProductToProductResponseMapper.initialize();

    private final CustomPageToCustomPagingResponseMapper customPageToCustomPagingResponseMapper =
            CustomPageToCustomPagingResponseMapper.initialize();

    /**
     * Creates a new product. Requires ADMIN authority.
     *
     * @param productCreateRequest the request payload containing product details
     * @return a {@link CustomResponse} containing the ID of the created product
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN')") // Keep this for protected endpoint
    public CustomResponse<String> createProduct(@RequestBody @Valid final ProductCreateRequest productCreateRequest) {

        final Product createdProduct = productCreateService
                .createProduct(productCreateRequest);

        return CustomResponse.successOf(createdProduct.getId());
    }

    /**
     * Retrieves a product by its ID. Publicly accessible.
     *
     * @param productId the ID of the product to retrieve
     * @return a {@link CustomResponse} containing the product details
     */
    @GetMapping("/{productId}")
    // @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')") // Removed, now handled by SecurityConfig permitAll
    public CustomResponse<ProductResponse> getProductById(@PathVariable @UUID final String productId) {

        final Product product = productReadService.getProductById(productId);
        final ProductResponse productResponse = productToProductResponseMapper.map(product);
        return CustomResponse.successOf(productResponse);
    }

    /**
     * Retrieves a paginated list of products based on the paging request. Publicly accessible.
     *
     * @param productPagingRequest the request payload containing paging information
     * @return a {@link CustomResponse} containing the paginated list of products
     */
    @GetMapping
    // @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')") // Removed, now handled by SecurityConfig permitAll
    public CustomResponse<CustomPagingResponse<ProductResponse>> getProducts(
            @RequestParam(name = "page", defaultValue = "1") int page,         // 1-based page number from client
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", required = false) String sortBy,     // e.g., "name,asc"
            @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice
            // Add other filter parameters here as @RequestParam if needed
    ) {
        // Construct the ProductPagingRequest DTO from query parameters
        ProductPagingRequest productPagingRequest = new ProductPagingRequest();
        CustomPaging pagination = new CustomPaging();
        pagination.setPageNumber(page); // CustomPaging internally converts to 0-based if needed by PageRequest
        pagination.setPageSize(size);
        productPagingRequest.setPagination(pagination);

        if (sortBy != null && !sortBy.isEmpty()) {
            productPagingRequest.setSortBy(sortBy);
        }
        if (maxPrice != null) {
            productPagingRequest.setMaxPrice(maxPrice);
        }

        // Now call your service with the constructed DTO
        final CustomPage<Product> productPage = productReadService.getProducts(productPagingRequest);
        final CustomPagingResponse<ProductResponse> productPagingResponse =
                customPageToCustomPagingResponseMapper.toPagingResponse(productPage);
        return CustomResponse.successOf(productPagingResponse);
    }

    /**
     * Updates an existing product by its ID. Requires ADMIN authority.
     *
     * @param productUpdateRequest the request payload containing updated product details
     * @param productId the ID of the product to update
     * @return a {@link CustomResponse} containing the updated product details
     */
    @PutMapping("/{productId}")
    @PreAuthorize("hasAnyAuthority('ADMIN')") // Keep this for protected endpoint
    public CustomResponse<ProductResponse> updatedProductById(
            @RequestBody @Valid final ProductUpdateRequest productUpdateRequest,
            @PathVariable @UUID final String productId) {

        final Product updatedProduct = productUpdateService.updateProductById(productId, productUpdateRequest);
        final ProductResponse productResponse = productToProductResponseMapper.map(updatedProduct);
        return CustomResponse.successOf(productResponse);
    }

    /**
     * Deletes a product by its ID. Requires ADMIN authority.
     *
     * @param productId the ID of the product to delete
     * @return a {@link CustomResponse} indicating successful deletion
     */
    @DeleteMapping("/{productId}")
    @PreAuthorize("hasAnyAuthority('ADMIN')") // Keep this for protected endpoint
    public CustomResponse<Void> deleteProductById(@PathVariable @UUID final String productId) {

        productDeleteService.deleteProductById(productId);
        return CustomResponse.SUCCESS;
    }

    /**
     * Retrieves all products belonging to a specific category. Publicly accessible.
     *
     * @param category the category to filter products by
     * @return a {@link CustomResponse} containing the list of products in the specified category
     */
    @GetMapping("/category/{category}")
    // @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')") // Removed, now handled by SecurityConfig permitAll
    public CustomResponse<List<ProductResponse>> getProductsByCategory(@PathVariable final String category) {

        final List<Product> productsByCategory = productReadService.getProductsByCategory(category);
        final List<ProductResponse> responses = productsByCategory.stream()
                .map(productToProductResponseMapper::map)
                .toList();
        return CustomResponse.successOf(responses);
    }
    /**
     * Processes a purchase request for a product. Requires authenticated user.
     *
     * @param productId the ID of the product to purchase
     * @param purchaseRequest the request payload containing purchase quantity
     * @return a {@link CustomResponse} containing the updated product details after purchase
     */
    @PostMapping("/{productId}/purchase")
    @PreAuthorize("isAuthenticated()") // Ensure user is logged in
    public CustomResponse<ProductResponse> purchaseProduct(
            @PathVariable @UUID final String productId,
            @RequestBody @Valid final PurchaseRequest purchaseRequest
    ) {
        final Product updatedProduct = productUpdateService.processPurchase(productId, purchaseRequest.getQuantity());
        final ProductResponse productResponse = productToProductResponseMapper.map(updatedProduct);
        return CustomResponse.successOf(productResponse);
    }
}