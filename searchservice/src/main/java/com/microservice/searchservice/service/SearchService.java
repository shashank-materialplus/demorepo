package com.microservice.searchservice.service;

import com.microservice.searchservice.dto.IndexableProduct;
import com.microservice.searchservice.dto.SearchRequest;
import com.microservice.searchservice.dto.SearchResponse;
import com.microservice.searchservice.dto.SuggestResponse;

public interface SearchService {
    SearchResponse search(SearchRequest request);
    boolean isServiceHealthy();
    // === NEW: Methods for indexing ===
    void indexDocument(IndexableProduct product);
    void deleteDocument(String documentId);

    SuggestResponse getSuggestions(String query);

    // =================================
}