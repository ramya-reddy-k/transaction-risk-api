package com.ramya.transactionrisk.migration;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(properties = {
        "spring.datasource.driver-class-name=org.postgresql.Driver",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.baseline-on-migrate=false"
})
@Testcontainers
class FlywayPostgreSqlMigrationIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    @Test
    void shouldApplyInitialMigrationToPostgreSql()
            throws Exception {

        try (Connection connection =
                     dataSource.getConnection()) {

            assertThat(
                    connection.getMetaData()
                            .getDatabaseProductName()
            ).isEqualTo("PostgreSQL");
        }

        Integer successfulMigrationCount =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM flyway_schema_history
                        WHERE version = '1'
                          AND success = TRUE
                        """,
                        Integer.class
                );

        assertThat(successfulMigrationCount)
                .isEqualTo(1);

        List<String> tables =
                jdbcTemplate.queryForList(
                        """
                        SELECT table_name
                        FROM information_schema.tables
                        WHERE table_schema = 'public'
                          AND table_name IN (
                              'transactions',
                              'outbox_events',
                              'risk_rules'
                          )
                        """,
                        String.class
                );

        assertThat(tables)
                .containsExactlyInAnyOrder(
                        "transactions",
                        "outbox_events",
                        "risk_rules"
                );
    }
}
