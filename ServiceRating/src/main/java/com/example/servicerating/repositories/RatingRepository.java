package com.example.servicerating.repositories;

import com.example.servicerating.data.Rating;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface RatingRepository extends ReactiveCrudRepository<Rating, String> {

    Flux<Rating> findAll();

    @Query
    Mono<Rating> findRatingByDescription( String description);


    void deleteAllById(String id);


    @Query
    Mono<Rating> findRatingByRate( Double rate);

    Mono<Void> deleteById(@Param("id")String id);
}
