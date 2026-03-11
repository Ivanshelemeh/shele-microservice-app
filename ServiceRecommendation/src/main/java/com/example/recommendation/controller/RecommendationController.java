package com.example.recommendation.controller;

import com.example.recommendation.dto.RecommendationDTO;
import com.example.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rest/api/v1/recommendation")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping(value = "/all", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Flux<RecommendationDTO>> getAllRatings() {
        Flux<RecommendationDTO> ratingDTOList = recommendationService.findAllRatings();
        return ResponseEntity.ok().body(ratingDTOList);
    }

    @GetMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Mono<RecommendationDTO>> getRecommendation(@PathVariable String id) {
        return ResponseEntity.ok().body(recommendationService.getByRecommID(id));
    }

    @GetMapping(value = "/rate/{rate}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Mono<RecommendationDTO>> getRatingDto(@PathVariable Double rate) {
        Mono<RecommendationDTO> ratingDTOMono = recommendationService.findByRate(rate);
        return ResponseEntity.ok().body(ratingDTOMono);
    }

    @PatchMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Mono<RecommendationDTO>> postRating(@RequestBody @Valid RecommendationDTO dto) {
        Mono<RecommendationDTO> rDMono = recommendationService.updateById(dto.recommendationId(), dto.rate());
        return ResponseEntity.accepted().body(rDMono);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeRating(@PathVariable String id) {
        Mono<Void> voidMono = recommendationService.removeById(id);
        return ResponseEntity.noContent().build();
    }
}
