package com.ramya.transactionrisk.outbox;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;

import com.ramya.transactionrisk.transaction.CreateTransactionRequest;
import com.ramya.transactionrisk.transaction.TransactionResponse;
import com.ramya.transactionrisk.transaction.TransactionService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class TransactionOutboxIntegrationTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private OutboxEventRepository outboxRepository;

    @Test
    void shouldPersistTransactionAndOutboxEventTogether() {
        CreateTransactionRequest request =
                new CreateTransactionRequest(
                        "outbox-customer-100",
                        new BigDecimal("7500.00"),
                        "USD",
                        "GB",
                        "GAMBLING",
                        Instant.parse("2026-07-11T02:30:00Z")
                );

        TransactionResponse response =
                transactionService.create(request);

        OutboxEventEntity event =
                outboxRepository
                        .findByAggregateId(response.id().toString())
                        .orElseThrow();

        assertThat(event.getStatus())
                .isEqualTo(OutboxStatus.PENDING);

        assertThat(event.getAttempts()).isZero();

        assertThat(event.getPayload())
                .contains(response.id().toString())
                .contains("outbox-customer-100")
                .contains("\"riskLevel\":\"HIGH\"");
    }
}
