package com.microservice.searchservice.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult {
    private String id;
    private String title;
    private String description;
    private String url;
    private String imageUrl;
    private Double price;
    private String category;
    private String author;
    private String publisher;
    private Double rating;
    private String isbn;
    private Map<String, Object> metadata;
    private String excerpt;
    private Double relevanceScore;
}