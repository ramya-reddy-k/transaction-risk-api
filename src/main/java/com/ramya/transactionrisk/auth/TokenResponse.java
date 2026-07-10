package com.ramya.transactionrisk.auth;

import java.time.Instant;

public record TokenResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        Instant expiresAt
) {
}
