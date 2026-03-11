package com.example.recommendation.repositories;

import com.example.recommendation.data.Recommendation;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface RecommendationRepository extends ReactiveCrudRepository<Recommendation, String> {

    Mono<Recommendation> findRecommendationById(String id);

    Mono<Recommendation> findByRate(Double rate);
}
