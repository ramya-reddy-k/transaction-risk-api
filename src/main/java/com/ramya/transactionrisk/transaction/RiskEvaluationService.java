package com.ramya.transactionrisk.transaction;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.ramya.transactionrisk.riskrule.RiskRuleEntity;
import com.ramya.transactionrisk.riskrule.RiskRuleService;

import org.springframework.stereotype.Service;

@Service
public class RiskEvaluationService {

    private final RiskRuleService riskRuleService;

    public RiskEvaluationService(
            RiskRuleService riskRuleService
    ) {
        this.riskRuleService = riskRuleService;
    }

    public RiskAssessment evaluate(
            CreateTransactionRequest request
    ) {
        int score = 0;
        List<String> reasons = new ArrayList<>();

        for (RiskRuleEntity rule :
                riskRuleService.findEnabledRules()) {

            if (matches(rule, request)) {
                score += rule.getScore();
                reasons.add(rule.getReason());
            }
        }

        score = Math.min(score, 100);

        if (reasons.isEmpty()) {
            reasons.add(
                    "No elevated-risk conditions detected"
            );
        }

        return new RiskAssessment(
                score,
                determineRiskLevel(score),
                List.copyOf(reasons)
        );
    }

    private boolean matches(
            RiskRuleEntity rule,
            CreateTransactionRequest request
    ) {
        return switch (rule.getCode()) {
            case HIGH_AMOUNT ->
                    matchesHighAmount(rule, request);

            case FOREIGN_COUNTRY ->
                    matchesForeignCountry(rule, request);

            case UNUSUAL_HOURS ->
                    matchesUnusualHours(rule, request);

            case HIGH_RISK_CATEGORY ->
                    matchesHighRiskCategory(rule, request);
        };
    }

    private boolean matchesHighAmount(
            RiskRuleEntity rule,
            CreateTransactionRequest request
    ) {
        BigDecimal threshold =
                new BigDecimal(rule.getParameterValue());

        return request.amount().compareTo(threshold) >= 0;
    }

    private boolean matchesForeignCountry(
            RiskRuleEntity rule,
            CreateTransactionRequest request
    ) {
        String homeCountry =
                rule.getParameterValue()
                        .toUpperCase(Locale.ROOT);

        return !homeCountry.equals(
                request.country()
                        .toUpperCase(Locale.ROOT)
        );
    }

    private boolean matchesUnusualHours(
            RiskRuleEntity rule,
            CreateTransactionRequest request
    ) {
        String[] values =
                rule.getParameterValue().split("-");

        int normalStartHour =
                Integer.parseInt(values[0]);

        int normalEndHour =
                Integer.parseInt(values[1]);

        int transactionHour =
                request.transactionTime()
                        .atZone(ZoneOffset.UTC)
                        .getHour();

        return transactionHour < normalStartHour
                || transactionHour >= normalEndHour;
    }

    private boolean matchesHighRiskCategory(
            RiskRuleEntity rule,
            CreateTransactionRequest request
    ) {
        Set<String> configuredCategories =
                Arrays.stream(
                                rule.getParameterValue()
                                        .split(",")
                        )
                        .map(String::trim)
                        .filter(value ->
                                !value.isBlank()
                        )
                        .map(value ->
                                value.toUpperCase(
                                        Locale.ROOT
                                )
                        )
                        .collect(
                                java.util.stream.Collectors
                                        .toSet()
                        );

        String merchantCategory =
                request.merchantCategory()
                        .toUpperCase(Locale.ROOT);

        return configuredCategories.contains(
                merchantCategory
        );
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
