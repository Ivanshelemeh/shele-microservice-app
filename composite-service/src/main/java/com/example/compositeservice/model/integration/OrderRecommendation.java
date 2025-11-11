package com.example.compositeservice.model.integration;

import org.springframework.lang.NonNull;

public record OrderRecommendation(
        @NonNull
        String recommendationId,
        @NonNull
        String orderName,
        String content,
        Double orderRate
) {
}
