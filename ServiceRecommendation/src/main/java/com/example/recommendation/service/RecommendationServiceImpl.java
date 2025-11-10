package com.example.servicerating.service;

import com.example.servicerating.data.Recommendation;
import com.example.servicerating.dto.RecommendationDTO;
import com.example.servicerating.repositories.RecommendationRepository;
import com.example.servicerating.utils.Converter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
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

    @SneakyThrows
    @Override
    public Mono<RecommendationDTO> findByRate(@NotNull Double r) {
        return repo.findRatingByRate(r)
                .map(converter::convertFromEntity)
                .switchIfEmpty(Mono.empty());
    }

    @Override
    public Mono<Void> deleteByDescription(String des) {
        if (des == null ||des.isEmpty()) {
            log.debug("descprition not found {}",des);
            throw new IllegalArgumentException(" not such field exist");
        }
        return repo.deleteById(des)
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
    @SneakyThrows
    public void saveRDTO(RecommendationDTO r) {
        if (r != null) {
            Recommendation recommendation = converter.convertFromDTO(r);
            repo.save(recommendation);
        }
        log.debug("something wrong with RatingDto model");
        throw new RuntimeException("rating is  empty");

    }

    @Override
    public Mono<Void> removeById(@NotNull String id) {
        if (Objects.isNull(id)){
            log.debug("there is not such rate {}", id);
            throw new RuntimeException("Rate withn id is not exists");
        }
        repo.deleteAllById(id);
        return Mono.empty();
    }
}
