package com.springbootmicroservices.productservice.client;

import com.springbootmicroservices.productservice.client.dto.IndexableProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign client for communicating with the Search Service to manage the search index.
 */
// The name "search-service" must match the spring.application.name in its application.yml
@FeignClient(name = "search-service", path = "/api/v1/index")
public interface SearchServiceClient {

    /**
     * Pushes a product to the search service to be created or updated.
     * Corresponds to: PUT /api/v1/index/products
     * @param productDto The product data to be indexed.
     */
    @PutMapping("/products")
    void indexProduct(@RequestBody IndexableProductDto productDto);

    /**
     * Deletes a product from the search index.
     * Corresponds to: DELETE /api/v1/index/products/{documentId}
     * @param documentId The ID of the document/product to delete.
     */
    @DeleteMapping("/products/{documentId}")
    void deleteProduct(@PathVariable("documentId") String documentId);
}