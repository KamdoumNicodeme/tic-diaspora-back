package com.ticdiaspora.infrastructure.adapter.in.web.dto;

import com.ticdiaspora.domain.model.enums.ContributionStatus;
import com.ticdiaspora.domain.model.enums.PaymentMethod;
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
            String memberFullName,
            long expectedAmount,
            long paidAmount,
            long remainingAmount,
            String currency,
            ContributionStatus status,
            Instant paidAt,
            UUID validatedBy,
            Instant validatedAt,
            PaymentMethod lastPaymentMethod,
            String lastTransactionReference,
            String lastProofUrl,
            Instant lastPaymentAt
    ) {
    }
}
