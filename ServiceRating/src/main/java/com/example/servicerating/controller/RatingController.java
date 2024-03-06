package com.example.servicerating.controller;

import com.example.servicerating.dto.RatingDTO;
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
@RequestMapping("/rating")
public class RatingController {

    private final RatingServiceImp ratingServiceImp;

    @GetMapping(value = "/all", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Flux<RatingDTO>> getAllRatings() {
        Flux<RatingDTO> ratingDTOList = ratingServiceImp.findAllRatings();
        return ResponseEntity.ok().body(ratingDTOList);
    }

    @GetMapping(value = "/{rate}",produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Mono<RatingDTO>> getRatingDto(@PathVariable Double rate){
        Mono<RatingDTO> ratingDTOMono = ratingServiceImp.findByRate(rate);
        return ResponseEntity.ok().body(ratingDTOMono);
    }

    @PatchMapping(consumes = APPLICATION_JSON_VALUE,produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Mono<RatingDTO>> postRating(@RequestBody @Valid RatingDTO dto){
        Mono<RatingDTO> rDMono = ratingServiceImp.updateById(dto.getId());
        return ResponseEntity.accepted().body(rDMono);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeRating(String id){
       Mono<Void>  voidMono = ratingServiceImp.removeById(id);
        return ResponseEntity.noContent().build();
    }
}
