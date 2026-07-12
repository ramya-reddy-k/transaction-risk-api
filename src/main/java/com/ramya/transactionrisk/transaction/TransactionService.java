package com.ramya.transactionrisk.transaction;

import com.ramya.transactionrisk.outbox.TransactionOutboxService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    private static final int MAX_PAGE_SIZE = 100;

    private final TransactionRepository transactionRepository;
    private final RiskEvaluationService riskEvaluationService;
    private final TransactionOutboxService transactionOutboxService;

    public TransactionService(
            TransactionRepository transactionRepository,
            RiskEvaluationService riskEvaluationService,
            TransactionOutboxService transactionOutboxService
    ) {
        this.transactionRepository = transactionRepository;
        this.riskEvaluationService = riskEvaluationService;
        this.transactionOutboxService = transactionOutboxService;
    }

    @Transactional
    public TransactionResponse create(CreateTransactionRequest request) {
        RiskAssessment assessment = riskEvaluationService.evaluate(request);

        TransactionEntity entity = new TransactionEntity(
                request.customerId().trim(),
                request.amount(),
                request.currency().toUpperCase(Locale.ROOT),
                request.country().toUpperCase(Locale.ROOT),
                request.merchantCategory().toUpperCase(Locale.ROOT),
                request.transactionTime(),
                assessment.score(),
                assessment.level(),
                assessment.reasons()
        );

        TransactionEntity saved =
                transactionRepository.save(entity);

        transactionOutboxService.recordTransactionCreated(saved);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public TransactionResponse getById(UUID id) {
        TransactionEntity entity = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));

        return toResponse(entity);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> search(
            String customerId,
            RiskLevel riskLevel,
            String country,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            Instant from,
            Instant to,
            Pageable pageable
    ) {
        validateSearch(
                country,
                minAmount,
                maxAmount,
                from,
                to,
                pageable
        );

        Specification<TransactionEntity> specification =
                (root, query, criteriaBuilder) ->
                        criteriaBuilder.conjunction();

        if (customerId != null && !customerId.isBlank()) {
            String normalizedCustomerId = customerId.trim();

            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.equal(
                                    root.get("customerId"),
                                    normalizedCustomerId
                            )
            );
        }

        if (riskLevel != null) {
            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.equal(
                                    root.get("riskLevel"),
                                    riskLevel
                            )
            );
        }

        if (country != null && !country.isBlank()) {
            String normalizedCountry =
                    country.toUpperCase(Locale.ROOT);

            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.equal(
                                    root.get("country"),
                                    normalizedCountry
                            )
            );
        }

        if (minAmount != null) {
            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.greaterThanOrEqualTo(
                                    root.get("amount"),
                                    minAmount
                            )
            );
        }

        if (maxAmount != null) {
            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.lessThanOrEqualTo(
                                    root.get("amount"),
                                    maxAmount
                            )
            );
        }

        if (from != null) {
            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.greaterThanOrEqualTo(
                                    root.get("transactionTime"),
                                    from
                            )
            );
        }

        if (to != null) {
            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.lessThanOrEqualTo(
                                    root.get("transactionTime"),
                                    to
                            )
            );
        }

        return transactionRepository
                .findAll(specification, pageable)
                .map(this::toResponse);
    }

    private void validateSearch(
            String country,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            Instant from,
            Instant to,
            Pageable pageable
    ) {
        if (country != null
                && !country.isBlank()
                && country.length() != 2) {
            throw new IllegalArgumentException(
                    "Country must use a two-letter code"
            );
        }

        if (minAmount != null
                && minAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Minimum amount cannot be negative"
            );
        }

        if (maxAmount != null
                && maxAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Maximum amount cannot be negative"
            );
        }

        if (minAmount != null
                && maxAmount != null
                && minAmount.compareTo(maxAmount) > 0) {
            throw new IllegalArgumentException(
                    "Minimum amount cannot exceed maximum amount"
            );
        }

        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException(
                    "From timestamp cannot be after to timestamp"
            );
        }

        if (pageable.getPageSize() > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException(
                    "Page size cannot exceed " + MAX_PAGE_SIZE
            );
        }
    }

    private TransactionResponse toResponse(
            TransactionEntity entity
    ) {
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
