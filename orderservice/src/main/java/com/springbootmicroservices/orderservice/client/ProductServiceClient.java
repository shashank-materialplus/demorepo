package com.springbootmicroservices.orderservice.client;

import com.springbootmicroservices.orderservice.config.FeignClientConfig;
import com.springbootmicroservices.orderservice.config.ProductServiceFeignConfig;

// Import the DTOs we created in the .dto package
import com.springbootmicroservices.orderservice.client.dto.ProductDetailsDto;
import com.springbootmicroservices.orderservice.client.dto.PurchaseQuantityDto;
import com.springbootmicroservices.orderservice.client.dto.CustomResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody; // Added for reduceStock
// import org.springframework.web.bind.annotation.RequestParam; // No longer needed for reduceStock if quantity is in body

// Commented out these inline records as we have proper DTO classes now
// record ProductDetailsDto(String id, String name, BigDecimal unitPrice, Integer currentStock) {}
// record StockAdjustmentRequest(String productId, int quantityChange) {}

@FeignClient(name = "productservice", path = "/api/v1/products", configuration = ProductServiceFeignConfig.class)
public interface ProductServiceClient {

    /**
     * Retrieves essential product details (like price and current stock) by its ID from ProductService.
     * This method expects ProductService to have an endpoint that returns a ProductDetailsDto structure.
     * This could be the standard GET /{productId} endpoint if its ProductResponse can be mapped
     * by Feign to ProductDetailsDto, or a dedicated endpoint like /{productId}/details-for-order.
     *
     * @param productId the ID of the product to retrieve
     * @return ResponseEntity containing product details needed for order processing
     */
    // Option 1: Assuming ProductService's standard GET /{productId} returns enough info compatible with ProductDetailsDto
    @GetMapping("/{productId}")
    CustomResponse<ProductDetailsDto> getProductDetails(@PathVariable("productId") String productId);
    // Option 2: If ProductService has a specific endpoint for order-related details
    // @GetMapping("/{productId}/details-for-order")
    // ResponseEntity<ProductDetailsDto> getProductDetailsForOrder(@PathVariable("productId") String productId);


    /**
     * Reduces the stock for a single product in ProductService.
     * This corresponds to the POST /api/v1/products/{productId}/purchase endpoint in ProductService,
     * which expects a request body containing the quantity.
     *
     * @param productId the ID of the product whose stock is to be reduced
     * @param quantityDto DTO containing the quantity to reduce stock by
     * @return ResponseEntity indicating success or failure (e.g., Void for 200/204 on success)
     */
    @PostMapping(path = "/{productId}/purchase", consumes = MediaType.APPLICATION_JSON_VALUE)
    void reduceStock(@PathVariable("productId") String productId, @RequestBody PurchaseQuantityDto quantityDto);

    // Example: Batch stock update (more efficient if purchasing multiple different items)
    // ProductService would need to implement an endpoint for this, and you'd need a DTO for the list.
    // @PostMapping("/stock/batch-update")
    // ResponseEntity<Void> batchUpdateStock(@RequestBody List<com.springbootmicroservices.orderservice.client.dto.StockAdjustmentDto> requests);
    // where StockAdjustmentDto might be:
    // package com.springbootmicroservices.orderservice.client.dto;
    // public record StockAdjustmentDto(String productId, int quantityChange) {}
}