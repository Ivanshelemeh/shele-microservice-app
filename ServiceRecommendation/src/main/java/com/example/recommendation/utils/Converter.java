package com.example.recommendation.utils;

import com.example.recommendation.data.Recommendation;
import com.example.recommendation.dto.RecommendationDTO;
import org.springframework.stereotype.Component;

@Component
public class Converter {

    public RecommendationDTO convertFromEntity(Recommendation rec) {
        return new RecommendationDTO(rec.getId(), rec.getRate(), rec.getDescription());
    }

    public Recommendation convertFromDTO(RecommendationDTO dto) {
        return new Recommendation(dto.recommendationId(), dto.description(), dto.rate());
    }
}
