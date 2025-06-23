package com.microservice.searchservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FacetValue {
    private String value;
    private long count;
    private String state; // e.g., "selected", "idle"
}
