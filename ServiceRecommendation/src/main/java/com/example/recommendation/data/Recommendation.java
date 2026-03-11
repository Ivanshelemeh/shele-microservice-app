package com.example.recommendation.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;


@NoArgsConstructor
@AllArgsConstructor
@Document("recommendation")
public class Recommendation {

    @MongoId
    private String id;

    @NotNull
    private String description;

    @NotNull
    @PositiveOrZero
    private Double rate;
}
