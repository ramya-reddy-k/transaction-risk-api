package com.ramya.transactionrisk.common.api;

public record FieldViolation(
        String field,
        String message
) {
}
