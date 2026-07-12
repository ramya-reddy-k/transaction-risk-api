package com.ramya.transactionrisk.outbox;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ramya.transactionrisk.transaction.TransactionEntity;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionOutboxService {

    private static final String AGGREGATE_TYPE = "TRANSACTION";
    private static final String EVENT_TYPE = "TransactionCreated.v1";

    private final OutboxEventRepository repository;
    private final ObjectMapper objectMapper;

    public TransactionOutboxService(
            OutboxEventRepository repository,
            ObjectMapper objectMapper
    ) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void recordTransactionCreated(
            TransactionEntity transaction
    ) {
        UUID eventId = UUID.randomUUID();

        TransactionCreatedEvent event =
                new TransactionCreatedEvent(
                        eventId,
                        transaction.getId(),
                        transaction.getCustomerId(),
                        transaction.getAmount(),
                        transaction.getCurrency(),
                        transaction.getCountry(),
                        transaction.getMerchantCategory(),
                        transaction.getRiskScore(),
                        transaction.getRiskLevel(),
                        transaction.getRiskReasons(),
                        transaction.getTransactionTime(),
                        transaction.getCreatedAt()
                );

        OutboxEventEntity outboxEvent =
                new OutboxEventEntity(
                        eventId,
                        AGGREGATE_TYPE,
                        transaction.getId().toString(),
                        EVENT_TYPE,
                        serialize(event)
                );

        repository.save(outboxEvent);
    }

    @Transactional
    public List<OutboxMessage> claimBatch(
            int maximumAttempts,
            Duration staleProcessingTimeout
    ) {
        repository.recoverStaleProcessingEvents(
                OutboxStatus.PROCESSING,
                OutboxStatus.FAILED,
                Instant.now().minus(staleProcessingTimeout),
                "Recovered stale processing event"
        );

        List<OutboxEventEntity> events =
                repository
                        .findTop20ByStatusInAndAttemptsLessThanOrderByCreatedAtAsc(
                                Set.of(
                                        OutboxStatus.PENDING,
                                        OutboxStatus.FAILED
                                ),
                                maximumAttempts
                        );

        events.forEach(OutboxEventEntity::markProcessing);

        return events.stream()
                .map(event -> new OutboxMessage(
                        event.getId(),
                        event.getAggregateId(),
                        event.getPayload()
                ))
                .toList();
    }

    @Transactional
    public void markPublished(UUID eventId) {
        repository.findById(eventId)
                .ifPresent(OutboxEventEntity::markPublished);
    }

    @Transactional
    public void markFailed(
            UUID eventId,
            String failureMessage
    ) {
        repository.findById(eventId)
                .ifPresent(event ->
                        event.markFailed(failureMessage)
                );
    }

    private String serialize(
            TransactionCreatedEvent event
    ) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Unable to serialize transaction event",
                    exception
            );
        }
    }
}
