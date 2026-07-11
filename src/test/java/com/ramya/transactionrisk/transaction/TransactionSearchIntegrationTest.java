package com.ramya.transactionrisk.transaction;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

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
class TransactionSearchIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSearchAndPageTransactions() throws Exception {
        String token = obtainToken();

        createTransaction(
                token,
                "search-customer-high",
                "8000.00",
                "GB",
                "GAMBLING",
                "2026-07-10T02:30:00Z"
        );

        createTransaction(
                token,
                "search-customer-low",
                "125.50",
                "US",
                "GROCERY",
                "2026-07-10T15:30:00Z"
        );

        mockMvc.perform(
                        get("/api/v1/transactions")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .param(
                                        "customerId",
                                        "search-customer-high"
                                )
                                .param("riskLevel", "HIGH")
                                .param("country", "GB")
                                .param("page", "0")
                                .param("size", "5")
                                .param(
                                        "sort",
                                        "createdAt,desc"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.totalElements")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.content[0].customerId")
                                .value("search-customer-high")
                )
                .andExpect(
                        jsonPath("$.content[0].riskLevel")
                                .value("HIGH")
                );
    }

    @Test
    void shouldReturnStructuredErrorForInvalidRange()
            throws Exception {
        String token = obtainToken();

        mockMvc.perform(
                        get("/api/v1/transactions")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .param("minAmount", "9000")
                                .param("maxAmount", "100")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(
                        jsonPath("$.error")
                                .value("Bad Request")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Minimum amount cannot exceed maximum amount"
                                )
                )
                .andExpect(
                        jsonPath("$.path")
                                .value("/api/v1/transactions")
                );
    }

    @Test
    void shouldReturnStructuredNotFoundError()
            throws Exception {
        String token = obtainToken();
        UUID missingId = UUID.randomUUID();

        mockMvc.perform(
                        get(
                                "/api/v1/transactions/{id}",
                                missingId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(
                        jsonPath("$.error")
                                .value("Not Found")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Transaction not found: "
                                                + missingId
                                )
                );
    }

    private String obtainToken() throws Exception {
        MvcResult result = mockMvc.perform(
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
                .andReturn();

        JsonNode tokenJson = objectMapper.readTree(
                result.getResponse().getContentAsString()
        );

        return tokenJson.get("accessToken").asText();
    }

    private void createTransaction(
            String token,
            String customerId,
            String amount,
            String country,
            String merchantCategory,
            String transactionTime
    ) throws Exception {
        String request = """
                {
                  "customerId": "%s",
                  "amount": %s,
                  "currency": "USD",
                  "country": "%s",
                  "merchantCategory": "%s",
                  "transactionTime": "%s"
                }
                """.formatted(
                        customerId,
                        amount,
                        country,
                        merchantCategory,
                        transactionTime
                );

        mockMvc.perform(
                        post("/api/v1/transactions")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isCreated());
    }
}
