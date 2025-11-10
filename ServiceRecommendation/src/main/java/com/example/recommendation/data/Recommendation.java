package com.example.recommendation.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("recommendation")
public class Recommendation {

    @MongoId
    private String id;

    @NotNull
    @Field("description")
    private String description;

    @Field("rate")
    @NotNull
    @PositiveOrZero
    private Double orderRate;

    public Recommendation of() {
        return new Recommendation( id,  description, orderRate);
    }


}
