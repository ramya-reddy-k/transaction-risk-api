package com.ramya.transactionrisk.outbox;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OutboxEventRepository
        extends JpaRepository<OutboxEventEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<OutboxEventEntity>
    findTop20ByStatusInAndAttemptsLessThanOrderByCreatedAtAsc(
            Collection<OutboxStatus> statuses,
            int maximumAttempts
    );

    Optional<OutboxEventEntity> findByAggregateId(
            String aggregateId
    );

    @Modifying
    @Query("""
            update OutboxEventEntity event
               set event.status = :failedStatus,
                   event.processingStartedAt = null,
                   event.lastError = :message
             where event.status = :processingStatus
               and event.processingStartedAt < :cutoff
            """)
    int recoverStaleProcessingEvents(
            @Param("processingStatus")
            OutboxStatus processingStatus,

            @Param("failedStatus")
            OutboxStatus failedStatus,

            @Param("cutoff")
            Instant cutoff,

            @Param("message")
            String message
    );
}
