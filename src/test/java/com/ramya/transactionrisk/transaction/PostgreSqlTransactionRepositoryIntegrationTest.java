package com.ramya.transactionrisk.transaction;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest(properties = {
        "spring.datasource.driver-class-name=org.postgresql.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@Testcontainers
class PostgreSqlTransactionRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DataSource dataSource;

    @Test
    void shouldPersistAndRetrieveTransactionUsingPostgreSql()
            throws Exception {

        TransactionEntity transaction =
                new TransactionEntity(
                        "postgres-customer-100",
                        new BigDecimal("2500.75"),
                        "USD",
                        "US",
                        "GROCERY",
                        Instant.parse("2026-07-12T15:30:00Z"),
                        10,
                        RiskLevel.LOW,
                        List.of(
                                "No elevated-risk conditions detected"
                        )
                );

        TransactionEntity saved =
                transactionRepository.saveAndFlush(transaction);

        UUID transactionId = saved.getId();

        entityManager.clear();

        TransactionEntity retrieved =
                transactionRepository.findById(transactionId)
                        .orElseThrow();

        assertThat(
                dataSource.getConnection()
                        .getMetaData()
                        .getDatabaseProductName()
        ).isEqualTo("PostgreSQL");

        assertThat(retrieved.getId())
                .isEqualTo(transactionId);

        assertThat(retrieved.getCustomerId())
                .isEqualTo("postgres-customer-100");

        assertThat(retrieved.getAmount())
                .isEqualByComparingTo("2500.75");

        assertThat(retrieved.getCurrency())
                .isEqualTo("USD");

        assertThat(retrieved.getCountry())
                .isEqualTo("US");

        assertThat(retrieved.getRiskLevel())
                .isEqualTo(RiskLevel.LOW);

        assertThat(retrieved.getRiskReasons())
                .containsExactly(
                        "No elevated-risk conditions detected"
                );
    }
}
