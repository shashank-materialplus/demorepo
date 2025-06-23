package com.microservice.searchservice.adapter;

import com.microservice.searchservice.client.CoveoApiClient;
import com.microservice.searchservice.config.SearchConfig;
import com.microservice.searchservice.dto.*;
import com.microservice.searchservice.dto.coveo.CoveoSearchRequest;
import com.microservice.searchservice.dto.coveo.CoveoSearchResponse;
import com.microservice.searchservice.dto.coveo.CoveoSuggestResponse;
import com.microservice.searchservice.exception.SearchException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class CoveoSearchAdapter implements SearchAdapter {

    private final CoveoApiClient coveoApiClient;
    private final SearchConfig.CoveoProperties coveoProperties;

    @Override
    public SearchResponse search(SearchRequest request) {
        try {
            log.info("Executing search with query: {}", request.getQuery());
            CoveoSearchRequest coveoRequest = transformToCovoRequest(request);
            CoveoSearchResponse coveoResponse = coveoApiClient.search(coveoRequest);
            return transformFromCoveoResponse(coveoResponse, request);
        } catch (Exception e) {
            log.error("Error executing search: {}", e.getMessage(), e);
            throw new SearchException("Failed to execute search", e);
        }
    }

    // =================================================================================
    // === THIS IS THE METHOD THAT IS NOW CORRECTED ====================================
    // =================================================================================
    @Override
    public SuggestResponse getSuggestions(String query) {
        try {
            log.info("Fetching suggestions for query: {}", query);
            // CORRECTED: This now correctly calls the coveoApiClient, not itself.
            CoveoSuggestResponse coveoResponse = coveoApiClient.getSuggestions(query, coveoProperties.getSearchHub());
            return transformToSuggestResponse(coveoResponse);
        } catch (Exception e) {
            log.error("Error fetching suggestions: {}", e.getMessage(), e);
            throw new SearchException("Failed to fetch suggestions", e);
        }
    }

    // --- All other methods below this line are correct and unchanged ---

    private CoveoSearchRequest transformToCovoRequest(SearchRequest request) {
        String aq = buildAqString(request.getCategories());
        String cq = buildCqString(request.getFilters());

        List<Map<String, Object>> groupBy = List.of(
                Map.of("field", "@category", "sortCriteria", "occurrences"),
                Map.of("field", "@author", "sortCriteria", "occurrences")
        );

        return CoveoSearchRequest.builder()
                .q(request.getQuery())
                .numberOfResults(request.getSize())
                .firstResult(request.getPage() * request.getSize())
                .sortCriteria(buildSortCriteria(request.getSortBy(), request.getSortOrder()))
                .aq(aq)
                .cq(cq)
                .groupBy(groupBy)
                .enableDidYouMean(true)
//                .searchHub(coveoProperties.getSearchHub())
                .build();
    }

    private String buildAqString(List<String> categories) {
        if (categories == null || categories.isEmpty()) return null;
        String joinedValues = categories.stream()
                .map(cat -> "\"" + cat.replace("\"", "\\\"") + "\"")
                .collect(Collectors.joining(","));
        return "(@category==(" + joinedValues + "))";
    }

    private String buildCqString(Map<String, String> filters) {
        if (filters == null || filters.isEmpty()) return null;
        return filters.entrySet().stream()
                .map(entry -> "(@" + entry.getKey() + "==\"" + entry.getValue().replace("\"", "\\\"") + "\")")
                .collect(Collectors.joining(" "));
    }

    @Override
    public void pushDocument(IndexableProduct product) {
        log.info("Pushing document with ID {} to Coveo.", product.getDocumentId());
        try {
            coveoApiClient.pushDocument(product);
        } catch (Exception e) {
            log.error("Error pushing document {}: {}", product.getDocumentId(), e.getMessage(), e);
            throw new SearchException("Failed to index document " + product.getDocumentId(), e);
        }
    }

    @Override
    public void deleteDocument(String documentId) {
        log.info("Deleting document with ID {} from Coveo.", documentId);
        try {
            coveoApiClient.deleteDocument(documentId);
        } catch (Exception e) {
            log.error("Error deleting document {}: {}", documentId, e.getMessage(), e);
            throw new SearchException("Failed to delete document " + documentId, e);
        }
    }

    @Override
    public boolean isHealthy() {
        return coveoApiClient.healthCheck();
    }

    @Override
    public String getProviderName() {
        return "Coveo";
    }

    private SearchResponse transformFromCoveoResponse(CoveoSearchResponse coveoResponse, SearchRequest request) {
        List<SearchResult> results = new ArrayList<>();
        if (coveoResponse.getResults() != null) {
            results = coveoResponse.getResults().stream()
                    .map(this::transformCoveoResult)
                    .collect(Collectors.toList());
        }

        List<FacetResult> facets = new ArrayList<>();
        if (coveoResponse.getGroupByResults() != null) {
            facets = coveoResponse.getGroupByResults().stream()
                    .map(this::transformCoveoFacet)
                    .collect(Collectors.toList());
        }

        return SearchResponse.builder()
                .results(results)
                .totalResults(coveoResponse.getTotalCount())
                .facets(facets)
                .page(request.getPage())
                .size(request.getSize())
                .duration(coveoResponse.getDuration())
                .queryId(coveoResponse.getSearchUid())
                .build();
    }

    private FacetResult transformCoveoFacet(CoveoSearchResponse.GroupByResult coveoFacet) {
        List<FacetValue> values = new ArrayList<>();
        if (coveoFacet.getValues() != null) {
            values = coveoFacet.getValues().stream()
                    .map(v -> FacetValue.builder()
                            .value(v.getValue())
                            .count(v.getNumberOfResults())
                            .state(v.getState())
                            .build())
                    .collect(Collectors.toList());
        }

        return FacetResult.builder()
                .field(coveoFacet.getField().replace("@", ""))
                .values(values)
                .build();
    }

    private SearchResult transformCoveoResult(CoveoSearchResponse.CoveoResult coveoResult) {
        Map<String, Object> raw = coveoResult.getRaw();
        return SearchResult.builder().id(coveoResult.getUniqueId()).title(coveoResult.getTitle()).description(coveoResult.getExcerpt()).url(coveoResult.getClickUri()).imageUrl(extractFromRaw(raw, "image", String.class)).price(extractFromRaw(raw, "price", Double.class)).category(extractFromRaw(raw, "category", String.class)).author(extractFromRaw(raw, "author", String.class)).publisher(extractFromRaw(raw, "publisher", String.class)).rating(extractFromRaw(raw, "rating", Double.class)).isbn(extractFromRaw(raw, "isbn", String.class)).metadata(raw).excerpt(coveoResult.getExcerpt()).relevanceScore(coveoResult.getScore()).build();
    }

    private String buildSortCriteria(String sortBy, String sortOrder) {
        if (sortBy == null || sortBy.isEmpty()) {
            return "relevancy";
        }
        String direction = "DESC".equalsIgnoreCase(sortOrder) ? " descending" : " ascending";
        return "@" + sortBy + direction;
    }

    private SuggestResponse transformToSuggestResponse(CoveoSuggestResponse coveoResponse) {
        if (coveoResponse == null || coveoResponse.getCompletions() == null) {
            return SuggestResponse.builder().completions(List.of()).build();
        }
        List<SuggestResponse.Completion> completions = coveoResponse.getCompletions().stream().map(coveoCompletion -> SuggestResponse.Completion.builder().expression(coveoCompletion.getExpression()).score(coveoCompletion.getScore()).build()).collect(Collectors.toList());
        return SuggestResponse.builder().completions(completions).build();
    }

    @SuppressWarnings("unchecked")
    private <T> T extractFromRaw(Map<String, Object> raw, String key, Class<T> type) {
        if (raw == null || !raw.containsKey(key)) return null;
        Object value = raw.get(key);
        if (type.isInstance(value)) return (T) value;
        if (type == Double.class && value instanceof String) {
            try {
                return (T) Double.valueOf((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}