package com.ramya.transactionrisk.transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        String customerId,
        BigDecimal amount,
        String currency,
        String country,
        String merchantCategory,
        Instant transactionTime,
        int riskScore,
        RiskLevel riskLevel,
        List<String> riskReasons,
        Instant createdAt
) {
}
