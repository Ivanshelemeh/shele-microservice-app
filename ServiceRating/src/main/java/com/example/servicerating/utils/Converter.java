package com.example.servicerating.utils;

import com.example.servicerating.data.Rating;
import com.example.servicerating.dto.RatingDTO;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Component;

@Component
public class Converter {

    public static final ModelMapper MODEL_MAPPER = new ModelMapper();

    public  RatingDTO convertFromEntity(Rating rating){
        return MODEL_MAPPER.map(rating, RatingDTO.class);
    }

    public Rating convertFromDTO(RatingDTO ratingDTO){
        MODEL_MAPPER.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return MODEL_MAPPER.map(ratingDTO,Rating.class);

    }
}
