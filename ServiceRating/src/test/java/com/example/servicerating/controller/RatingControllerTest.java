package com.example.servicerating.controller;

import com.example.servicerating.dto.RatingDTO;
import com.example.servicerating.repositories.RatingRepository;
import com.example.servicerating.service.RatingServiceImp;
import com.example.servicerating.utils.Converter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@WebFluxTest(RatingController.class)
class RatingControllerTest {

    @MockBean
    private RatingServiceImp ratingServiceImp;

    @MockBean
    private Converter converter;
    @MockBean
    private RatingRepository repository;

    @Autowired
    private WebTestClient webTestClient;

    private RatingDTO dto;

    @BeforeEach
    void setUp() {
        dto = new RatingDTO();
        dto.setRate(22.55);
        dto.setDescription("Jess full");
        dto.setId("222");
        repository.save(converter.convertFromDTO(dto));
    }

    //Test that checks RatingDtos should not empty and work as expected
    @Test
    public void testGetAll_RatingsDto() {

        List<RatingDTO> dtoList = new ArrayList<RatingDTO>();
        dtoList.add(dto);

        Flux<RatingDTO> flux = Flux.fromIterable(dtoList);

        when(ratingServiceImp.findAllRatings()).thenReturn(flux);

        webTestClient.get().uri("/rating/all")
                .exchange()
                .expectStatus().isOk();
        StepVerifier.create(flux)
                .recordWith(ArrayList::new)
                .thenConsumeWhile(x -> true)
                .expectRecordedMatches(elements -> !elements.isEmpty())
                .verifyComplete();
    }
    @Test
    public void test_Should_return_Dto_By_Rate(){

        when(ratingServiceImp.findByRate(anyDouble())).thenReturn(Mono.just(dto));

        webTestClient.get().uri("/rating/{rate}",dto.getRate())
                .exchange()
                .expectStatus()
                .isOk();
        StepVerifier.create(Mono.just(dto))
                .expectNext()
                .expectNextMatches(r->r.getRate()!= 0.0)
                .expectComplete();
    }

    @Test
    public void test_Should_return_Dto_updated(){
        dto.setDescription("vfbttr");
        repository.save(converter.convertFromDTO(dto));
        given(ratingServiceImp.updateById(anyString())).willReturn(Mono.just(dto));

        webTestClient.patch().uri("/rating").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus()
                .isAccepted();
        StepVerifier.create(Mono.just(dto))
                .expectNext()
                .expectNextMatches(d->!d.getDescription().isEmpty())
                .expectComplete();
    }
}