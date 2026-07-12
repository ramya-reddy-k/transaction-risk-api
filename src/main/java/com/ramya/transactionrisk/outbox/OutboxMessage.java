package com.ramya.transactionrisk.outbox;

import java.util.UUID;

public record OutboxMessage(
        UUID id,
        String aggregateId,
        String payload
) {
}
