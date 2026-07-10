package com.ramya.transactionrisk.transaction;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.Test;

class RiskEvaluationServiceTest {

    private final RiskEvaluationService service = new RiskEvaluationService();

    @Test
    void shouldClassifyHighRiskTransaction() {
        CreateTransactionRequest request = new CreateTransactionRequest(
                "customer-100",
                new BigDecimal("8000.00"),
                "USD",
                "GB",
                "GAMBLING",
                Instant.parse("2026-07-10T02:30:00Z")
        );

        RiskAssessment assessment = service.evaluate(request);

        assertThat(assessment.score()).isEqualTo(100);
        assertThat(assessment.level()).isEqualTo(RiskLevel.HIGH);
        assertThat(assessment.reasons()).hasSize(4);
    }

    @Test
    void shouldClassifyLowRiskTransaction() {
        CreateTransactionRequest request = new CreateTransactionRequest(
                "customer-101",
                new BigDecimal("125.50"),
                "USD",
                "US",
                "GROCERY",
                Instant.parse("2026-07-10T15:30:00Z")
        );

        RiskAssessment assessment = service.evaluate(request);

        assertThat(assessment.score()).isZero();
        assertThat(assessment.level()).isEqualTo(RiskLevel.LOW);
        assertThat(assessment.reasons())
                .containsExactly("No elevated-risk conditions detected");
    }
}
