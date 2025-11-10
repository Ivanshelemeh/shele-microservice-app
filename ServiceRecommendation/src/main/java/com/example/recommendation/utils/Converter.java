package com.example.servicerating.utils;

import com.example.servicerating.data.Recommendation;
import com.example.servicerating.dto.RecommendationDTO;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Component;

@Component
public class Converter {

    public static final ModelMapper MODEL_MAPPER = new ModelMapper();

    public RecommendationDTO convertFromEntity(Recommendation recommendation){
        return MODEL_MAPPER.map(recommendation, RecommendationDTO.class);
    }

    public Recommendation convertFromDTO(RecommendationDTO recommendationDTO){
        MODEL_MAPPER.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return MODEL_MAPPER.map(recommendationDTO, Recommendation.class);

    }
}
