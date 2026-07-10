package com.ramya.transactionrisk.transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "transactions")
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, length = 2)
    private String country;

    @Column(nullable = false)
    private String merchantCategory;

    @Column(nullable = false)
    private Instant transactionTime;

    @Column(nullable = false)
    private int riskScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskLevel riskLevel;

    @Column(nullable = false, length = 2000)
    private String riskReasons;

    @Column(nullable = false)
    private Instant createdAt;

    protected TransactionEntity() {
    }

    public TransactionEntity(
            String customerId,
            BigDecimal amount,
            String currency,
            String country,
            String merchantCategory,
            Instant transactionTime,
            int riskScore,
            RiskLevel riskLevel,
            List<String> riskReasons
    ) {
        this.customerId = customerId;
        this.amount = amount;
        this.currency = currency;
        this.country = country;
        this.merchantCategory = merchantCategory;
        this.transactionTime = transactionTime;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.riskReasons = String.join("|", riskReasons);
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getCountry() {
        return country;
    }

    public String getMerchantCategory() {
        return merchantCategory;
    }

    public Instant getTransactionTime() {
        return transactionTime;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public List<String> getRiskReasons() {
        return Arrays.stream(riskReasons.split("\\|"))
                .toList();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
