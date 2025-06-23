package com.microservice.searchservice.adapter;

import com.microservice.searchservice.dto.IndexableProduct;
import com.microservice.searchservice.dto.SearchRequest;
import com.microservice.searchservice.dto.SearchResponse;
import com.microservice.searchservice.dto.SuggestResponse;

/**
 * Search Adapter Interface - Defines the contract for search adapters
 * This allows for multiple search provider implementations (Coveo, Elasticsearch, etc.)
 */
public interface SearchAdapter {
    SearchResponse search(SearchRequest request);
    boolean isHealthy();
    String getProviderName();

    // === NEW: Methods for indexing ===
    void pushDocument(IndexableProduct product);
    void deleteDocument(String documentId);

    SuggestResponse getSuggestions(String query);

    // =================================

}