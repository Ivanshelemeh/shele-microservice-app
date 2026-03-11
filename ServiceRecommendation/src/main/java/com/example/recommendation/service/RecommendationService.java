package com.example.recommendation.service;

import com.example.recommendation.dto.RecommendationDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RecommendationService {

    Flux<RecommendationDTO> findAllRatings();

    Mono<RecommendationDTO> findByRate(Double rate);

    Mono<RecommendationDTO> getByRecommID(String recommendationId);

    Mono<RecommendationDTO> updateById(String id, Double newRate);

    void saveRDTO(RecommendationDTO recommendationDTO);

    Mono<Void> removeById(String id);
}
