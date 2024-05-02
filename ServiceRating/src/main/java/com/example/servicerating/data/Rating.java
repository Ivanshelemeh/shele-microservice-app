package com.example.servicerating.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("rate")
public class Rating {

    @MongoId
    private String id;

    @NotNull
    @Field("description")
    private String description;
    @Field("rate")
    @NotNull
    @PositiveOrZero
    private double rate;

    public Rating of() {
        return new Rating( id,  description, rate);
    }


}
