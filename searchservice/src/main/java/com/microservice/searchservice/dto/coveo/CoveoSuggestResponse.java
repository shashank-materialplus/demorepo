package com.microservice.searchservice.dto.coveo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CoveoSuggestResponse {
    private List<CoveoCompletion> completions;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CoveoCompletion {
        private String expression;
        private double score;
        private String highlighted;
    }
}
