package com.microservice.searchservice.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse {
    private List<SearchResult> results;
    private long totalResults;
    private int page;
    private int size;
    private long duration;
    private String queryId;
    // === NEW ===
    private List<FacetResult> facets;
    // ===========
}