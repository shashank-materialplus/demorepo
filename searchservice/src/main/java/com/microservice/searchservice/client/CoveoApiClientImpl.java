package com.microservice.searchservice.client;

import com.microservice.searchservice.config.SearchConfig;
import com.microservice.searchservice.dto.IndexableProduct;
import com.microservice.searchservice.dto.coveo.CoveoSearchRequest;
import com.microservice.searchservice.dto.coveo.CoveoSearchResponse;
import com.microservice.searchservice.dto.coveo.CoveoSuggestResponse;
import com.microservice.searchservice.exception.SearchException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// FINAL, GUARANTEED VERSION WITH TWO-KEY STRATEGY
@Component
@Slf4j
public class CoveoApiClientImpl implements CoveoApiClient {

    private final WebClient webClient;
    private final SearchConfig.CoveoProperties coveoProperties;
    private static final String PRODUCT_BASE_URI = "https://democoveo.com/products/";

    public CoveoApiClientImpl(WebClient.Builder webClientBuilder, SearchConfig.CoveoProperties coveoProperties) {
        this.coveoProperties = coveoProperties;
        // The WebClient is now generic, with no default Authorization header.
        this.webClient = webClientBuilder
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }


    @Override
    public void pushDocument(IndexableProduct product) {
        String documentUri = PRODUCT_BASE_URI + product.getDocumentId();
        URI uri = UriComponentsBuilder.fromHttpUrl(coveoProperties.getPushApiEndpoint())
                .path("/organizations/{orgId}/sources/{sourceId}/documents")
                .queryParam("documentId", documentUri)
                .build(coveoProperties.getOrganizationId(), coveoProperties.getSourceId());

        String searchableContent = Stream.of(product.getTitle(), product.getDescription(), product.getCategory(), product.getAuthor())
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining(" "));

        Map<String, Object> coveoPayload = new HashMap<>();
        coveoPayload.put("DocumentId", documentUri);
        coveoPayload.put("Title", product.getTitle());
        coveoPayload.put("FileExtension", ".json");
        coveoPayload.put("Data", searchableContent);
        coveoPayload.put("image", product.getImageUrl());
        coveoPayload.put("category", product.getCategory());
        coveoPayload.put("author", product.getAuthor());
        coveoPayload.put("price", product.getPrice());
        coveoPayload.put("stock", product.getStock());
        if (product.getCreatedAt() != null) {
            coveoPayload.put("created_date", product.getCreatedAt().toString());
        }

        log.info("Attempting PUSH operation with PUSH_KEY for document: {}", documentUri);

        webClient.put()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + coveoProperties.getPushApiKey()) // Use the Push Key
                .bodyValue(coveoPayload)
                .retrieve()
                .onStatus(status -> status.isError(), response ->
                        response.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new SearchException("Failed to push document to Coveo. Status: " + response.statusCode() + ", Body: " + errorBody)))
                )
                .toBodilessEntity()
                .retryWhen(Retry.backoff(coveoProperties.getMaxRetries(), Duration.ofSeconds(1)))
                .doOnSuccess(response -> log.info("SUCCESS: Document {} accepted by Coveo.", documentUri))
                .block();
    }

    @Override
    public void deleteDocument(String documentId) {
        String documentUri = PRODUCT_BASE_URI + documentId;
        URI uri = UriComponentsBuilder.fromHttpUrl(coveoProperties.getPushApiEndpoint())
                .path("/organizations/{orgId}/sources/{sourceId}/documents")
                .queryParam("documentId", documentUri)
                .build(coveoProperties.getOrganizationId(), coveoProperties.getSourceId());

        log.info("Attempting DELETE operation with PUSH_KEY for document: {}", documentUri);

        webClient.delete()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + coveoProperties.getPushApiKey()) // Use the Push Key
                .retrieve()
                .onStatus(status -> status.isError(), response -> Mono.error(new SearchException("Failed to delete document from Coveo. Status: " + response.statusCode())))
                .toBodilessEntity()
                .retryWhen(Retry.backoff(coveoProperties.getMaxRetries(), Duration.ofSeconds(1)))
                .block();
    }

    @Override
    public CoveoSearchResponse search(CoveoSearchRequest request) {
        log.info("Attempting SEARCH operation with SEARCH_KEY.");
        // === START: ADD THIS DEBUG BLOCK ===
        try {
            String searchKey = coveoProperties.getSearchApiKey();
            String searchEndpoint = coveoProperties.getSearchEndpoint();
            log.info("====== SEARCH DEBUG ======");
            log.info("Endpoint URL: {}", searchEndpoint);
            log.info("Using Search Key starting with: {}", searchKey.substring(0, Math.min(8, searchKey.length())));
            log.info("Request Body: {}", request.toString());
            log.info("==========================");
        } catch (Exception e) {
            log.error("CRITICAL ERROR: Failed to read configuration before making search call.", e);
        }
        // === END: ADD THIS DEBUG BLOCK ===
        return webClient.post()
                .uri(coveoProperties.getSearchEndpoint())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + coveoProperties.getSearchApiKey()) // Use the Search Key
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.isError(), response -> Mono.error(new SearchException("Error from Coveo Search API. Status: " + response.statusCode())))
                .bodyToMono(CoveoSearchResponse.class)
                .retryWhen(Retry.backoff(coveoProperties.getMaxRetries(), Duration.ofSeconds(1)))
                .block();
    }

    @Override
    public boolean healthCheck() {
        // The health check performs a search, so it uses the search method.
        try {
            CoveoSearchRequest healthRequest = CoveoSearchRequest.builder().q("test").numberOfResults(0).build();
            // This will internally call our corrected search() method, which uses the search key.
            CoveoSearchResponse response = this.search(healthRequest);
            return response != null;
        } catch (Exception e) {
            log.warn("Coveo health check failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public CoveoSuggestResponse getSuggestions(String query, String searchHub) {
        log.info("Attempting SUGGEST operation for query '{}' using searchHub '{}'", query, searchHub);

        String suggestEndpoint = coveoProperties.getSearchEndpoint() + "/querySuggest";

        // We are now correctly building the URI and adding the searchHub parameter.
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(suggestEndpoint)
                .queryParam("q", query)
                .queryParam("organizationId", coveoProperties.getOrganizationId())
                .queryParam("count", 5);

        // This block now ensures the searchHub is added to the URL.
        if (searchHub != null && !searchHub.isBlank()) {
            builder.queryParam("searchHub", searchHub);
        } else {
            log.error("FATAL: searchHub is NULL or empty. The Coveo Suggest API will fail. Check application.yml.");
            throw new IllegalArgumentException("SearchHub cannot be null or empty for suggestions.");
        }

        URI uri = builder.build().toUri();

        log.info("Final Suggest URL being called: {}", uri);

        return webClient.get()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + coveoProperties.getSearchApiKey())
                .retrieve()
                .onStatus(status -> status.isError(), response ->
                        response.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error("Coveo Suggest API Error! Status: {}, Body: {}", response.statusCode(), errorBody);
                                    return Mono.error(new SearchException("Failed to get suggestions from Coveo. Status: " + response.statusCode()));
                                })
                )
                .bodyToMono(CoveoSuggestResponse.class)
                .retryWhen(Retry.backoff(coveoProperties.getMaxRetries(), Duration.ofSeconds(1)))
                .block();
    }
}