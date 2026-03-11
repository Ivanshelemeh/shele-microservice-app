package com.example.recommendation.controller;

import com.example.recommendation.dto.RecommendationDTO;
import com.example.recommendation.service.RecommendationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebFluxTest(RecommendationController.class)
class RecommendationControllerTest {

    @MockBean
    private RecommendationService recommendationService;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void should_returnAllRatings_when_getAllRatingsEndpointCalled() {
        RecommendationDTO dto = new RecommendationDTO("222", 22.55, "Jess full");

        when(recommendationService.findAllRatings()).thenReturn(Flux.just(dto));

        webTestClient.get().uri("/rest/api/v1/recommendation/all")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void should_returnDto_when_getByRateCalled() {
        RecommendationDTO dto = new RecommendationDTO("222", 22.55, "Jess full");

        when(recommendationService.findByRate(anyDouble())).thenReturn(Mono.just(dto));

        webTestClient.get().uri("/rest/api/v1/recommendation/rate/{rate}", 22.55)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void should_returnUpdatedDto_when_patchCalled() {
        RecommendationDTO dto = new RecommendationDTO("222", 22.55, "Jess full");

        when(recommendationService.updateById(anyString(), anyDouble())).thenReturn(Mono.just(dto));

        webTestClient.patch().uri("/rest/api/v1/recommendation")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isAccepted();
    }

    @Test
    void should_returnNoContent_when_deleteCalled() {
        when(recommendationService.removeById(anyString())).thenReturn(Mono.empty());

        webTestClient.delete().uri("/rest/api/v1/recommendation/{id}", "222")
                .exchange()
                .expectStatus().isNoContent();
    }
}
