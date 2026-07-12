package com.ramya.transactionrisk.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.ramya.transactionrisk.riskrule.RiskRuleCode;
import com.ramya.transactionrisk.riskrule.RiskRuleEntity;
import com.ramya.transactionrisk.riskrule.RiskRuleService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RiskEvaluationServiceTest {

    @Mock
    private RiskRuleService riskRuleService;

    private RiskEvaluationService service;

    @BeforeEach
    void setUp() {
        service = new RiskEvaluationService(
                riskRuleService
        );

        when(riskRuleService.findEnabledRules())
                .thenReturn(defaultRules());
    }

    @Test
    void shouldClassifyHighRiskTransaction() {
        CreateTransactionRequest request =
                new CreateTransactionRequest(
                        "customer-100",
                        new BigDecimal("8000.00"),
                        "USD",
                        "GB",
                        "GAMBLING",
                        Instant.parse(
                                "2026-07-10T02:30:00Z"
                        )
                );

        RiskAssessment assessment =
                service.evaluate(request);

        assertThat(assessment.score())
                .isEqualTo(100);

        assertThat(assessment.level())
                .isEqualTo(RiskLevel.HIGH);

        assertThat(assessment.reasons())
                .hasSize(4);
    }

    @Test
    void shouldClassifyLowRiskTransaction() {
        CreateTransactionRequest request =
                new CreateTransactionRequest(
                        "customer-101",
                        new BigDecimal("125.50"),
                        "USD",
                        "US",
                        "GROCERY",
                        Instant.parse(
                                "2026-07-10T15:30:00Z"
                        )
                );

        RiskAssessment assessment =
                service.evaluate(request);

        assertThat(assessment.score()).isZero();

        assertThat(assessment.level())
                .isEqualTo(RiskLevel.LOW);

        assertThat(assessment.reasons())
                .containsExactly(
                        "No elevated-risk conditions detected"
                );
    }

    @Test
    void shouldIgnoreDisabledRules() {
        when(riskRuleService.findEnabledRules())
                .thenReturn(List.of());

        CreateTransactionRequest request =
                new CreateTransactionRequest(
                        "customer-102",
                        new BigDecimal("9000.00"),
                        "USD",
                        "GB",
                        "GAMBLING",
                        Instant.parse(
                                "2026-07-10T02:30:00Z"
                        )
                );

        RiskAssessment assessment =
                service.evaluate(request);

        assertThat(assessment.score()).isZero();

        assertThat(assessment.level())
                .isEqualTo(RiskLevel.LOW);
    }

    private List<RiskRuleEntity> defaultRules() {
        return List.of(
                new RiskRuleEntity(
                        RiskRuleCode.HIGH_AMOUNT,
                        true,
                        40,
                        "5000.00",
                        "Transaction amount exceeds the configured threshold"
                ),
                new RiskRuleEntity(
                        RiskRuleCode.FOREIGN_COUNTRY,
                        true,
                        20,
                        "US",
                        "Transaction originated outside the configured home country"
                ),
                new RiskRuleEntity(
                        RiskRuleCode.UNUSUAL_HOURS,
                        true,
                        15,
                        "6-23",
                        "Transaction occurred during unusual hours"
                ),
                new RiskRuleEntity(
                        RiskRuleCode.HIGH_RISK_CATEGORY,
                        true,
                        25,
                        "CASH_ADVANCE,GAMBLING,CRYPTO",
                        "Merchant category has elevated risk"
                )
        );
    }
}
