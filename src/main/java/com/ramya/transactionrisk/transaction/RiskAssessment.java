package com.ramya.transactionrisk.transaction;

import java.util.List;

public record RiskAssessment(
        int score,
        RiskLevel level,
        List<String> reasons
) {
}
