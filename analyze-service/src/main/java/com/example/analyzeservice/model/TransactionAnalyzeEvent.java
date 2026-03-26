package com.example.analyzeservice.model;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Validated
public record TransactionAnalyzeEvent(
        @NotNull
        String transactionId,
        @NotNull
        String customerId,
        @PositiveOrZero
        BigDecimal totalAmount,
        @NotNull
        String transactionStatus,
        @NotNull
        OffsetDateTime createAt,
        @Nullable
        BigDecimal amountRub,
        @Nullable
        String currency,
        @Nullable
        String paymentMethod

) {
}
