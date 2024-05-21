package com.example.servicerating.service;

import com.example.servicerating.data.Rating;
import com.example.servicerating.dto.RatingDTO;
import com.example.servicerating.repositories.RatingRepository;
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
public class RatingServiceImp implements RatingService {

    private final Converter converter;
    private final RatingRepository repo;

    @Override
    public Flux<RatingDTO> findAllRatings() {
        return repo.findAll()
                .filter(r -> r.getRate() != 0)
                .map(converter::convertFromEntity)
                .switchIfEmpty(Flux.empty());
    }

    @SneakyThrows
    @Override
    public Mono<RatingDTO> findByRate(@NotNull Double r) {
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
    public Mono<RatingDTO> updateById(String id) {
        return repo.findById(id)
                .doOnNext(e -> e.setId(id))
                .flatMap(repo::save)
                .map(converter::convertFromEntity)
                .switchIfEmpty(Mono.empty());
    }

    @Override
    @SneakyThrows
    public void saveRDTO(RatingDTO r) {
        if (r != null) {
            Rating rating = converter.convertFromDTO(r);
            repo.save(rating);
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
