package com.ramya.transactionrisk.riskrule;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RiskRuleRepository
        extends JpaRepository<RiskRuleEntity, UUID> {

    Optional<RiskRuleEntity> findByCode(RiskRuleCode code);

    List<RiskRuleEntity> findAllByOrderByCodeAsc();

    List<RiskRuleEntity> findAllByEnabledTrueOrderByCodeAsc();
}
