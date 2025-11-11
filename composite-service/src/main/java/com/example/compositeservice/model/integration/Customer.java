package com.example.compositeservice.model.integration;

import jakarta.validation.constraints.Positive;
import org.springframework.lang.NonNull;

public record Customer(
        @Positive
        Integer customerID,
        @NonNull
        String customerName
) {
}
