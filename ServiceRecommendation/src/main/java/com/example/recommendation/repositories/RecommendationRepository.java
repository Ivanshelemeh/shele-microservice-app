package com.example.servicerating.repositories;

import com.example.servicerating.data.Recommendation;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface RecommendationRepository extends ReactiveCrudRepository<Recommendation, String> {

    @Query
    Mono<Recommendation> findRecommendationById(String id);

    @Query
    Mono<Recommendation> findRecommendationByRate(Double rate);

    Mono<Void> deleteById(@Param("id")String id);
}
