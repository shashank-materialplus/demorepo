package com.microservice.searchservice.dto.coveo;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Map;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoveoSearchRequest {
    private String q;
    private int numberOfResults;
    private int firstResult;
    private String sortCriteria;

    // CORRECTED: These must be strings for the Coveo API, not complex objects.
    private String aq;
    private String cq;

    private boolean enableDidYouMean;
    private boolean enableQuerySyntax;
    private String pipeline;
    private String searchHub;
    private String tab;
    private String locale;
    private String timezone;
    private List<Map<String, Object>> groupBy;
}