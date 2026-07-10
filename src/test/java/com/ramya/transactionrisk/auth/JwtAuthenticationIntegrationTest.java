package com.ramya.transactionrisk.auth;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class JwtAuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldRejectInvalidCredentials() throws Exception {
        mockMvc.perform(post("/api/v1/auth/token")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "demo",
                                  "password": "incorrect"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectTransactionWithoutBearerToken() throws Exception {
        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(APPLICATION_JSON)
                        .content(highRiskTransaction()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldIssueTokenAndAllowProtectedTransaction() throws Exception {
        MvcResult tokenResult = mockMvc.perform(
                        post("/api/v1/auth/token")
                                .contentType(APPLICATION_JSON)
                                .content("""
                                        {
                                          "username": "demo",
                                          "password": "TestPassword123!"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresInSeconds").value(900))
                .andReturn();

        JsonNode tokenJson = objectMapper.readTree(
                tokenResult.getResponse().getContentAsString()
        );

        String accessToken = tokenJson
                .get("accessToken")
                .asText();

        mockMvc.perform(post("/api/v1/transactions")
                        .header(
                                "Authorization",
                                "Bearer " + accessToken
                        )
                        .contentType(APPLICATION_JSON)
                        .content(highRiskTransaction()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value("customer-100"))
                .andExpect(jsonPath("$.riskLevel").value("HIGH"))
                .andExpect(jsonPath("$.riskScore").value(100));
    }

    private String highRiskTransaction() {
        return """
                {
                  "customerId": "customer-100",
                  "amount": 8000.00,
                  "currency": "USD",
                  "country": "GB",
                  "merchantCategory": "GAMBLING",
                  "transactionTime": "2026-07-10T02:30:00Z"
                }
                """;
    }
}
