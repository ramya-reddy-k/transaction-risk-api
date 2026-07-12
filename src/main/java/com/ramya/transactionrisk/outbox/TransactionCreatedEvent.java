package com.ramya.transactionrisk.outbox;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.ramya.transactionrisk.transaction.RiskLevel;

public record TransactionCreatedEvent(
        UUID eventId,
        UUID transactionId,
        String customerId,
        BigDecimal amount,
        String currency,
        String country,
        String merchantCategory,
        int riskScore,
        RiskLevel riskLevel,
        List<String> riskReasons,
        Instant transactionTime,
        Instant createdAt
) {
}
