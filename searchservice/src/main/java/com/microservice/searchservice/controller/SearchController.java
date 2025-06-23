package com.microservice.searchservice.controller;

import com.microservice.searchservice.dto.SearchRequest;
import com.microservice.searchservice.dto.SearchResponse;
import com.microservice.searchservice.dto.SuggestResponse;
import com.microservice.searchservice.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import org.springframework.validation.annotation.Validated; // === NEW IMPORT ===

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Tag(name = "Search", description = "Search API for book catalog")
@Validated
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    @Operation(summary = "Search books", description = "Search for books using various parameters")
    public ResponseEntity<SearchResponse> search(
            @Parameter(description = "Search query") @RequestParam("q") String query,
            @Parameter(description = "Page number") @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Categories") @RequestParam(value = "categories", required = false) List<String> categories,
            @Parameter(description = "Sort by field") @RequestParam(value = "sortBy", required = false) String sortBy,
            @Parameter(description = "Sort order") @RequestParam(value = "sortOrder", defaultValue = "DESC") String sortOrder,
            @Parameter(description = "Additional filters") @RequestParam(required = false) Map<String, String> filters) {

        if (filters != null) {
            filters.remove("q");
            filters.remove("page");
            filters.remove("size");
            filters.remove("categories");
            filters.remove("sortBy");
            filters.remove("sortOrder");
        }

        SearchRequest request = SearchRequest.builder()
                .query(query)
                .page(page)
                .size(size)
                .categories(categories)
                .sortBy(sortBy)
                .sortOrder(sortOrder)
                .filters(filters)
                .build();

        SearchResponse response = searchService.search(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/suggest")
    @Operation(summary = "Get search suggestions", description = "Provides search-as-you-type suggestions for a given partial query.")
    public ResponseEntity<SuggestResponse> suggest(
            @Parameter(description = "Partial search query (min 2 characters)")
            @RequestParam("q")
            @Size(min = 2, message = "Query must be at least 2 characters long")
            String query) {
        SuggestResponse response = searchService.getSuggestions(query);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if search service is healthy")
    public ResponseEntity<Map<String, Object>> health() {
        boolean healthy = searchService.isServiceHealthy();
        Map<String, Object> status = Map.of(
                "status", healthy ? "UP" : "DOWN",
                "service", "search-service",
                "provider", "Coveo"
        );

        return healthy ? ResponseEntity.ok(status) : ResponseEntity.status(503).body(status);
    }
}