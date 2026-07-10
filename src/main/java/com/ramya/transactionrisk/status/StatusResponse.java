package com.ramya.transactionrisk.status;

import java.time.Instant;

public record StatusResponse(
        String application,
        String status,
        Instant timestamp
) {
}
