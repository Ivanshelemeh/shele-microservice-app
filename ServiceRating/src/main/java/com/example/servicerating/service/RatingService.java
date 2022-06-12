package com.example.servicerating.service;

import com.example.servicerating.data.Rating;
import com.example.servicerating.dto.RatingDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RatingService {
    Flux<RatingDTO> findAllRatings();

    Mono<RatingDTO> findByRate(Double r);

    Mono<Void> deleteByDescription(String des);

    Mono<RatingDTO> updateById(String id);

    void saveRDTO(RatingDTO ratingDTOMono);

}
