package com.microservice.searchservice.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class FacetResult {
    private String field;
    private List<FacetValue> values;
}