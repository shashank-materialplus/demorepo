package com.microservice.searchservice.controller;

import com.microservice.searchservice.dto.IndexableProduct;
import com.microservice.searchservice.service.SearchService; // Assuming you add indexing methods here
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/index")
@RequiredArgsConstructor
@Tag(name = "Indexing", description = "API for synchronizing data with the search provider")
public class IndexController {

    private final SearchService searchService;

    @PutMapping("/products")
    @Operation(summary = "Index or update a product", description = "Adds or updates a product in the Coveo index.")
    public ResponseEntity<Void> indexProduct(@Valid @RequestBody IndexableProduct product) {
        searchService.indexDocument(product);
        return ResponseEntity.accepted().build();
    }

    @DeleteMapping("/products/{documentId}")
    @Operation(summary = "Delete a product", description = "Removes a product from the Coveo index.")
    public ResponseEntity<Void> deleteProduct(@PathVariable String documentId) {
        searchService.deleteDocument(documentId);
        return ResponseEntity.noContent().build();
    }
}