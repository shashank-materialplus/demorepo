package com.microservice.searchservice.dto.coveo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CoveoSearchResponse {
    private List<CoveoResult> results;
    private long totalCount;
    private long duration;
    private String indexDuration;
    private String requestDuration;
    private String searchUid;
    private boolean hasResults;
    // === NEW: For receiving facet data ===
    private List<GroupByResult> groupByResults;
    // ===================================

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CoveoResult {
        private String title;
        private String uri;
        private String clickUri;
        private String excerpt;
        private String firstSentences;
        private double score;
        private Map<String, Object> raw;
        private String uniqueId;
        private String printableUri;
        private String summary;
        private boolean isRecommendation;
        private boolean isTopResult;
    }

    // === NEW: Nested classes for facet (groupBy) results ===
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GroupByResult {
        private String field;
        private List<GroupByValue> values;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GroupByValue {
        private String value;
        private long numberOfResults;
        private String state; // e.g., "idle", "selected"
    }
    // ====================================================
}