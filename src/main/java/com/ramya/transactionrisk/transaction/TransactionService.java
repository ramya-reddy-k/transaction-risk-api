package com.ramya.transactionrisk.transaction;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final RiskEvaluationService riskEvaluationService;

    public TransactionService(
            TransactionRepository transactionRepository,
            RiskEvaluationService riskEvaluationService
    ) {
        this.transactionRepository = transactionRepository;
        this.riskEvaluationService = riskEvaluationService;
    }

    @Transactional
    public TransactionResponse create(CreateTransactionRequest request) {
        RiskAssessment assessment = riskEvaluationService.evaluate(request);

        TransactionEntity entity = new TransactionEntity(
                request.customerId(),
                request.amount(),
                request.currency(),
                request.country(),
                request.merchantCategory(),
                request.transactionTime(),
                assessment.score(),
                assessment.level(),
                assessment.reasons()
        );

        return toResponse(transactionRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public TransactionResponse getById(UUID id) {
        TransactionEntity entity = transactionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Transaction not found"
                ));

        return toResponse(entity);
    }

    private TransactionResponse toResponse(TransactionEntity entity) {
        return new TransactionResponse(
                entity.getId(),
                entity.getCustomerId(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getCountry(),
                entity.getMerchantCategory(),
                entity.getTransactionTime(),
                entity.getRiskScore(),
                entity.getRiskLevel(),
                entity.getRiskReasons(),
                entity.getCreatedAt()
        );
    }
}
