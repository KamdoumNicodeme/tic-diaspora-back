package com.ticdiaspora.penalty.infrastructure.web;

import com.ticdiaspora.shared.domain.enums.PenaltyStatus;
import com.ticdiaspora.shared.domain.enums.PenaltyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;
import java.util.UUID;

public final class PenaltyDtos {
    private PenaltyDtos() {
    }

    public record PenaltyRequest(
            @NotNull UUID memberId,
            UUID meetingId,
            @NotNull PenaltyType type,
            @Positive long amount,
            @NotBlank String reason
    ) {
    }

    public record WaivePenaltyRequest(@NotBlank String reason) {
    }

    public record PenaltyResponse(
            UUID id,
            UUID memberId,
            UUID meetingId,
            PenaltyType type,
            long amount,
            PenaltyStatus status,
            String reason,
            Instant createdAt,
            Instant paidAt,
            UUID validatedBy,
            String cancellationReason
    ) {
    }
}
