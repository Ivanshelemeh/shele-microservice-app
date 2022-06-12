package com.example.servicerating.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import javax.validation.constraints.NotNull;

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
    private double rate;


}
