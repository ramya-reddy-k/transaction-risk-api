package com.ramya.transactionrisk.transaction;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

@Service
public class RiskEvaluationService {

    private static final BigDecimal HIGH_AMOUNT_THRESHOLD = new BigDecimal("5000.00");

    private static final Set<String> HIGH_RISK_CATEGORIES = Set.of(
            "CASH_ADVANCE",
            "GAMBLING",
            "CRYPTO"
    );

    public RiskAssessment evaluate(CreateTransactionRequest request) {
        int score = 0;
        List<String> reasons = new ArrayList<>();

        if (request.amount().compareTo(HIGH_AMOUNT_THRESHOLD) >= 0) {
            score += 40;
            reasons.add("Transaction amount exceeds the configured threshold");
        }

        if (!"US".equalsIgnoreCase(request.country())) {
            score += 20;
            reasons.add("Transaction originated outside the United States");
        }

        int hour = request.transactionTime()
                .atZone(ZoneOffset.UTC)
                .getHour();

        if (hour < 6 || hour >= 23) {
            score += 15;
            reasons.add("Transaction occurred during unusual hours");
        }

        if (HIGH_RISK_CATEGORIES.contains(request.merchantCategory().toUpperCase())) {
            score += 25;
            reasons.add("Merchant category has elevated risk");
        }

        score = Math.min(score, 100);

        RiskLevel level = determineRiskLevel(score);

        if (reasons.isEmpty()) {
            reasons.add("No elevated-risk conditions detected");
        }

        return new RiskAssessment(score, level, List.copyOf(reasons));
    }

    private RiskLevel determineRiskLevel(int score) {
        if (score >= 60) {
            return RiskLevel.HIGH;
        }

        if (score >= 30) {
            return RiskLevel.MEDIUM;
        }

        return RiskLevel.LOW;
    }
}
