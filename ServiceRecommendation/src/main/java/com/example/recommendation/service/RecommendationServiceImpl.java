package com.example.recommendation.service;

import com.example.recommendation.data.Recommendation;
import com.example.recommendation.dto.RecommendationDTO;
import com.example.recommendation.repositories.RecommendationRepository;
import com.example.recommendation.utils.Converter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotNull;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {

    private final Converter converter;
    private final RecommendationRepository repo;

    @Override
    public Flux<RecommendationDTO> findAllRatings() {
        return repo.findAll()
                .filter(r -> r.getRate() > 0.0)
                .map(converter::convertFromEntity)
                .switchIfEmpty(Flux.empty());
    }

    @Override
    public Mono<RecommendationDTO> findByRate(@NotNull Double r) {
        return repo.findByRate(r)
                .map(converter::convertFromEntity)
                .switchIfEmpty(Mono.empty());
    }

    @Override
    public Mono<RecommendationDTO> getByRecommID(String id) {
        return repo.findRecommendationById(id)
                .map(converter::convertFromEntity)
                .switchIfEmpty(Mono.empty());
    }

    @Override
    public Mono<RecommendationDTO> updateById(String id, Double newRate) {
        return repo.findById(id)
                .doOnNext(e -> e.setRate(newRate))
                .flatMap(repo::save)
                .map(converter::convertFromEntity)
                .switchIfEmpty(Mono.empty());
    }

    @Override
    public void saveRDTO(RecommendationDTO r) {
        if (r != null) {
            Recommendation recommendation = converter.convertFromDTO(r);
            repo.save(recommendation).subscribe();
            return;
        }
        log.debug("something wrong with RecommendationDTO model");
        throw new RuntimeException("recommendation DTO is empty");
    }

    @Override
    public Mono<Void> removeById(@NotNull String id) {
        if (Objects.isNull(id)) {
            log.debug("there is no such recommendation with id {}", id);
            throw new RuntimeException("Recommendation with id does not exist");
        }
        return repo.deleteById(id);
    }
}
