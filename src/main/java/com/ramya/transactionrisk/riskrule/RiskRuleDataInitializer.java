package com.ramya.transactionrisk.riskrule;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RiskRuleDataInitializer implements ApplicationRunner {

    private final RiskRuleRepository repository;

    public RiskRuleDataInitializer(
            RiskRuleRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments arguments) {
        createIfMissing(
                RiskRuleCode.HIGH_AMOUNT,
                40,
                "5000.00",
                "Transaction amount exceeds the configured threshold"
        );

        createIfMissing(
                RiskRuleCode.FOREIGN_COUNTRY,
                20,
                "US",
                "Transaction originated outside the configured home country"
        );

        createIfMissing(
                RiskRuleCode.UNUSUAL_HOURS,
                15,
                "6-23",
                "Transaction occurred during unusual hours"
        );

        createIfMissing(
                RiskRuleCode.HIGH_RISK_CATEGORY,
                25,
                "CASH_ADVANCE,GAMBLING,CRYPTO",
                "Merchant category has elevated risk"
        );
    }

    private void createIfMissing(
            RiskRuleCode code,
            int score,
            String parameterValue,
            String reason
    ) {
        if (repository.findByCode(code).isEmpty()) {
            repository.save(
                    new RiskRuleEntity(
                            code,
                            true,
                            score,
                            parameterValue,
                            reason
                    )
            );
        }
    }
}
