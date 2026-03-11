package com.example.compositeservice.model;

public record RecommendationSummary(
        Integer recommendationId,
        String content,
        Double rate
) {
}
