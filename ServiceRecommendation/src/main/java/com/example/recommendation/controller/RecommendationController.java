package com.example.servicerating.controller;

import com.example.servicerating.dto.RecommendationDTO;
import com.example.servicerating.service.RatingServiceImp;
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

    @GetMapping(value = "/{rate}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Mono<RecommendationDTO>> getRatingDto(@PathVariable Double rate) {
        Mono<RecommendationDTO> ratingDTOMono = recommendationService.findByRate(rate);
        return ResponseEntity.ok().body(ratingDTOMono);
    }

    @PatchMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Mono<RecommendationDTO>> postRating(@RequestBody @Valid RecommendationDTO dto) {
        Mono<RecommendationDTO> rDMono = ratingServiceImp.updateById(dto.getId());
        return ResponseEntity.accepted().body(rDMono);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeRating(String id) {
        Mono<Void> voidMono = ratingServiceImp.removeById(id);
        return ResponseEntity.noContent().build();
    }
}
