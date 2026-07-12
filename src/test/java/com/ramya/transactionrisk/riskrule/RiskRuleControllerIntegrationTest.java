package com.ramya.transactionrisk.riskrule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;

import com.ramya.transactionrisk.transaction.CreateTransactionRequest;
import com.ramya.transactionrisk.transaction.RiskAssessment;
import com.ramya.transactionrisk.transaction.RiskEvaluationService;
import com.ramya.transactionrisk.transaction.RiskLevel;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RiskRuleControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RiskRuleRepository riskRuleRepository;

    @Autowired
    private RiskEvaluationService riskEvaluationService;

    @Test
    void shouldRejectRequestWithoutAuthentication()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/admin/risk-rules")
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnConfiguredRiskRules()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/admin/risk-rules")
                                .with(jwt())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(
                        jsonPath("$[0].code")
                                .isNotEmpty()
                )
                .andExpect(
                        jsonPath("$[0].enabled")
                                .isBoolean()
                )
                .andExpect(
                        jsonPath("$[0].score")
                                .isNumber()
                )
                .andExpect(
                        jsonPath("$[0].parameterValue")
                                .isNotEmpty()
                );
    }

    @Test
    void shouldUpdateRuleAndUseItDuringEvaluation()
            throws Exception {

        mockMvc.perform(
                        put(
                                "/api/v1/admin/risk-rules/HIGH_AMOUNT"
                        )
                                .with(jwt())
                                .contentType(APPLICATION_JSON)
                                .content("""
                                        {
                                          "enabled": true,
                                          "score": 55,
                                          "parameterValue": "1000.00",
                                          "reason": "Configured amount threshold exceeded"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.code")
                                .value("HIGH_AMOUNT")
                )
                .andExpect(
                        jsonPath("$.enabled")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.score")
                                .value(55)
                )
                .andExpect(
                        jsonPath("$.parameterValue")
                                .value("1000.00")
                )
                .andExpect(
                        jsonPath("$.reason")
                                .value(
                                        "Configured amount threshold exceeded"
                                )
                );

        RiskRuleEntity updatedRule =
                riskRuleRepository
                        .findByCode(
                                RiskRuleCode.HIGH_AMOUNT
                        )
                        .orElseThrow();

        assertThat(updatedRule.getScore())
                .isEqualTo(55);

        assertThat(updatedRule.getParameterValue())
                .isEqualTo("1000.00");

        CreateTransactionRequest request =
                new CreateTransactionRequest(
                        "config-test-customer",
                        new BigDecimal("1500.00"),
                        "USD",
                        "US",
                        "GROCERY",
                        Instant.parse(
                                "2026-07-12T15:30:00Z"
                        )
                );

        RiskAssessment assessment =
                riskEvaluationService.evaluate(request);

        assertThat(assessment.score())
                .isEqualTo(55);

        assertThat(assessment.level())
                .isEqualTo(RiskLevel.MEDIUM);

        assertThat(assessment.reasons())
                .containsExactly(
                        "Configured amount threshold exceeded"
                );
    }
}
