package com.example.servicerating.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

@Data
@Accessors(chain = true)
public class RatingDTO {

    @NotNull
    private String id;
    @NotNull
    @PositiveOrZero
    private double rate;
    private String description;
}
