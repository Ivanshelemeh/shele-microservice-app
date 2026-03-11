package com.example.recommendation.dto;

import org.springframework.lang.NonNull;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;

public record RecommendationDTO(
        String recommendationId,
        @PositiveOrZero
        @NonNull
        Double rate,
        @NonNull
        @NotBlank
        String description
) {
}
