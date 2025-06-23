package com.microservice.searchservice.client;

import com.microservice.searchservice.dto.IndexableProduct;
import com.microservice.searchservice.dto.SuggestResponse;
import com.microservice.searchservice.dto.coveo.CoveoSearchRequest;
import com.microservice.searchservice.dto.coveo.CoveoSearchResponse;
import com.microservice.searchservice.dto.coveo.CoveoSuggestResponse;

// The corrected interface we are now using
public interface CoveoApiClient {
    CoveoSearchResponse search(CoveoSearchRequest request);
    boolean healthCheck();
    // highlight-start
    void pushDocument(IndexableProduct product); // Takes the whole product DTO
    // highlight-end
    void deleteDocument(String documentId);
    CoveoSuggestResponse getSuggestions(String query, String searchHub);
}