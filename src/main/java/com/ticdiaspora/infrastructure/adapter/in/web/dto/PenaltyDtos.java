package com.ticdiaspora.infrastructure.adapter.in.web.dto;

import com.ticdiaspora.domain.model.enums.PenaltyStatus;
import com.ticdiaspora.domain.model.enums.PenaltyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.Instant;
import java.util.UUID;

public final class PenaltyDtos {
    private PenaltyDtos() {
    }

    public record PenaltyRequest(
            @NotNull UUID memberId,
            UUID meetingId,
            @NotNull PenaltyType type,
            @PositiveOrZero long amount,
            @NotBlank String reason
    ) {
    }

    public record WaivePenaltyRequest(@NotBlank String reason) {
    }

    public record PenaltyResponse(
            UUID id,
            UUID memberId,
            String memberFullName,
            UUID meetingId,
            String meetingTitle,
            PenaltyType type,
            long amount,
            PenaltyStatus status,
            String reason,
            Instant createdAt,
            Instant paidAt,
            UUID validatedBy,
            String validatedByFullName,
            String cancellationReason
    ) {
    }

    public record PenaltySummary(
            long totalCount,
            long pendingCount,
            long paidCount,
            long waivedCount,
            long cancelledCount,
            long pendingAmount,
            long paidAmount,
            long waivedAmount,
            long cancelledAmount
    ) {
    }
}
