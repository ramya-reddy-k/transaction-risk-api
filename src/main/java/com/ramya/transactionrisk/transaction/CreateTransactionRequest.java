package com.ramya.transactionrisk.transaction;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateTransactionRequest(

        @NotBlank
        String customerId,

        @NotNull
        @Positive
        BigDecimal amount,

        @NotBlank
        @Pattern(regexp = "[A-Z]{3}", message = "Currency must use a three-letter uppercase code")
        String currency,

        @NotBlank
        @Size(min = 2, max = 2, message = "Country must use a two-letter code")
        String country,

        @NotBlank
        String merchantCategory,

        @NotNull
        Instant transactionTime
) {
}
