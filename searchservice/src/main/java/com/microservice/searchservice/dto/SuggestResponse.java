package com.microservice.searchservice.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuggestResponse {
    private List<Completion> completions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Completion {
        private String expression;
        private double score;
    }
}
