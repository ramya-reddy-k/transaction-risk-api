package com.ramya.transactionrisk.riskrule;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "risk_rules",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_risk_rule_code",
                        columnNames = "code"
                )
        }
)
public class RiskRuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private RiskRuleCode code;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false, length = 500)
    private String parameterValue;

    @Column(nullable = false, length = 255)
    private String reason;

    @Column(nullable = false)
    private Instant updatedAt;

    protected RiskRuleEntity() {
    }

    public RiskRuleEntity(
            RiskRuleCode code,
            boolean enabled,
            int score,
            String parameterValue,
            String reason
    ) {
        this.code = code;
        this.enabled = enabled;
        this.score = score;
        this.parameterValue = parameterValue;
        this.reason = reason;
    }

    public void update(
            boolean enabled,
            int score,
            String parameterValue,
            String reason
    ) {
        this.enabled = enabled;
        this.score = score;
        this.parameterValue = parameterValue;
        this.reason = reason;
    }

    @PrePersist
    @PreUpdate
    void updateTimestamp() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public RiskRuleCode getCode() {
        return code;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getScore() {
        return score;
    }

    public String getParameterValue() {
        return parameterValue;
    }

    public String getReason() {
        return reason;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
