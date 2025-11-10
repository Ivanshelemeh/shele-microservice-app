package com.example.servicerating.dto;

import org.springframework.lang.NonNull;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;

public record RecommendationDTO(
        @PositiveOrZero
        @NonNull
        Double orderRate,
        @NonNull
        @NotBlank
        String description
) {

}
