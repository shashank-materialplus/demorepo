package com.microservice.searchservice.service;

import com.microservice.searchservice.adapter.SearchAdapter;
import com.microservice.searchservice.dto.IndexableProduct;
import com.microservice.searchservice.dto.SearchRequest;
import com.microservice.searchservice.dto.SearchResponse;
import com.microservice.searchservice.dto.SuggestResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchServiceImpl implements SearchService {

    private final SearchAdapter searchAdapter;

    @Override
    public SearchResponse search(SearchRequest request) {
        log.info("Processing search request for query: '{}'", request.getQuery());
        validateSearchRequest(request);

        // Delegate to adapter
        SearchResponse response = searchAdapter.search(request);

        log.info("Search completed. Found {} results with {} facets.", response.getTotalResults(), response.getFacets().size());
        return response;
    }

    // === NEW: Implement indexing methods, delegating to the adapter ===
    @Override
    public void indexDocument(IndexableProduct product) {
        if (product.getDocumentId() == null || product.getDocumentId().isBlank()) {
            throw new IllegalArgumentException("Document ID cannot be null or empty for indexing.");
        }
        log.info("Processing index request for document ID: {}", product.getDocumentId());
        searchAdapter.pushDocument(product);
    }

    @Override
    public void deleteDocument(String documentId) {
        if (documentId == null || documentId.isBlank()) {
            throw new IllegalArgumentException("Document ID cannot be null or empty for deletion.");
        }
        log.info("Processing delete request for document ID: {}", documentId);
        searchAdapter.deleteDocument(documentId);
    }
    // ===============================================================

    @Override
    public boolean isServiceHealthy() {
        return searchAdapter.isHealthy();
    }

    @Override
    public SuggestResponse getSuggestions(String query) {
        if (query == null || query.trim().length() < 2) {
            // Avoid calling the API for very short or empty queries
            return SuggestResponse.builder().completions(List.of()).build();
        }
        log.info("Processing suggestions request for query: '{}'", query);
        return searchAdapter.getSuggestions(query);
    }
    private void validateSearchRequest(SearchRequest request) {
        if (request.getQuery() == null || request.getQuery().trim().isEmpty()) {
            throw new IllegalArgumentException("Search query cannot be empty");
        }

        if (request.getSize() > 100) {
            throw new IllegalArgumentException("Page size cannot exceed 100");
        }
    }
}