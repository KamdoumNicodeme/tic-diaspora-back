package com.ticdiaspora.contribution.infrastructure.web;

import com.ticdiaspora.shared.domain.enums.ContributionStatus;
import com.ticdiaspora.shared.domain.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;
import java.util.UUID;

public final class ContributionDtos {
    private ContributionDtos() {
    }

    public record ContributionPaymentRequest(
            @NotNull UUID cycleId,
            @NotNull UUID memberId,
            @Positive long amount,
            @NotNull PaymentMethod method,
            String transactionReference,
            String proofUrl
    ) {
    }

    public record ContributionResponse(
            UUID id,
            UUID cycleId,
            UUID memberId,
            long expectedAmount,
            long paidAmount,
            long remainingAmount,
            String currency,
            ContributionStatus status,
            Instant paidAt,
            UUID validatedBy,
            Instant validatedAt
    ) {
    }
}
