package com.ramya.transactionrisk.riskrule;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RiskRuleService {

    private final RiskRuleRepository repository;

    public RiskRuleService(RiskRuleRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<RiskRuleResponse> findAll() {
        return repository.findAllByOrderByCodeAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RiskRuleEntity> findEnabledRules() {
        return repository.findAllByEnabledTrueOrderByCodeAsc();
    }

    @Transactional
    public RiskRuleResponse update(
            RiskRuleCode code,
            UpdateRiskRuleRequest request
    ) {
        validateParameter(code, request.parameterValue());

        RiskRuleEntity rule = repository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Risk rule not found: " + code
                ));

        rule.update(
                request.enabled(),
                request.score(),
                normalizeParameter(code, request.parameterValue()),
                request.reason().trim()
        );

        return toResponse(rule);
    }

    private void validateParameter(
            RiskRuleCode code,
            String parameterValue
    ) {
        String value = parameterValue.trim();

        switch (code) {
            case HIGH_AMOUNT -> validateAmount(value);
            case FOREIGN_COUNTRY -> validateCountry(value);
            case UNUSUAL_HOURS -> validateHours(value);
            case HIGH_RISK_CATEGORY -> validateCategories(value);
        }
    }

    private void validateAmount(String value) {
        try {
            BigDecimal amount = new BigDecimal(value);

            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(
                        "High-amount threshold cannot be negative"
                );
            }
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "High-amount parameter must be a valid number"
            );
        }
    }

    private void validateCountry(String value) {
        if (!value.matches("[A-Za-z]{2}")) {
            throw new IllegalArgumentException(
                    "Home-country parameter must use a two-letter code"
            );
        }
    }

    private void validateHours(String value) {
        String[] parts = value.split("-");

        if (parts.length != 2) {
            throw new IllegalArgumentException(
                    "Unusual-hours parameter must use START-END format"
            );
        }

        try {
            int start = Integer.parseInt(parts[0]);
            int end = Integer.parseInt(parts[1]);

            if (start < 0 || start > 23
                    || end < 0 || end > 23
                    || start >= end) {
                throw new IllegalArgumentException(
                        "Unusual hours must be between 0 and 23 with START before END"
                );
            }
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Unusual-hours values must be whole numbers"
            );
        }
    }

    private void validateCategories(String value) {
        boolean hasCategory = Arrays.stream(value.split(","))
                .map(String::trim)
                .anyMatch(category -> !category.isBlank());

        if (!hasCategory) {
            throw new IllegalArgumentException(
                    "At least one high-risk category is required"
            );
        }
    }

    private String normalizeParameter(
            RiskRuleCode code,
            String parameterValue
    ) {
        String value = parameterValue.trim();

        return switch (code) {
            case FOREIGN_COUNTRY ->
                    value.toUpperCase(Locale.ROOT);

            case HIGH_RISK_CATEGORY ->
                    Arrays.stream(value.split(","))
                            .map(String::trim)
                            .filter(category -> !category.isBlank())
                            .map(category ->
                                    category.toUpperCase(Locale.ROOT)
                            )
                            .distinct()
                            .sorted()
                            .reduce(
                                    (left, right) -> left + "," + right
                            )
                            .orElseThrow();

            default -> value;
        };
    }

    private RiskRuleResponse toResponse(RiskRuleEntity rule) {
        return new RiskRuleResponse(
                rule.getId(),
                rule.getCode(),
                rule.isEnabled(),
                rule.getScore(),
                rule.getParameterValue(),
                rule.getReason(),
                rule.getUpdatedAt()
        );
    }
}
