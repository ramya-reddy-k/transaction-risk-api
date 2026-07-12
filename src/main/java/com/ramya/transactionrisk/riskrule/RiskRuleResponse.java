package com.ramya.transactionrisk.riskrule;

import java.time.Instant;
import java.util.UUID;

public record RiskRuleResponse(
        UUID id,
        RiskRuleCode code,
        boolean enabled,
        int score,
        String parameterValue,
        String reason,
        Instant updatedAt
) {
}
