package com.example.servicerating.service;

import com.example.servicerating.dto.RecommendationDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RecommendationService {
    Flux<RecommendationDTO> findAllRatings();

    Mono<RecommendationDTO> findByRate(Double r);

    Mono<RecommendationDTO> getByRecommID(String id);

    Mono<RecommendationDTO> updateById(String id, Double newRate);

    void saveRDTO(RecommendationDTO recommendationDTOMono);

    Mono<Void> removeById(String id);

    Mono<RecommendationDTO> getRecommendation(String orderId);

}
