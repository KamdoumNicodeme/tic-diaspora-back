package com.ticdiaspora.tontine.infrastructure.web;

import com.ticdiaspora.shared.domain.enums.TontineCycleStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public final class TontineDtos {
    private TontineDtos() {
    }

    public record TontineCycleRequest(
            @Min(1) @Max(12) int month,
            @Min(2020) int year,
            UUID beneficiaryId,
            String comment
    ) {
    }

    public record TontineCycleResponse(
            UUID id,
            int month,
            int year,
            UUID beneficiaryId,
            TontineCycleStatus status,
            long expectedAmount,
            long collectedAmount,
            long transferredAmount,
            long remainingAmount,
            Instant openedAt,
            Instant closedAt,
            String comment
    ) {
    }

    public record ChangeBeneficiaryRequest(@NotNull UUID beneficiaryId, String reason) {
    }
}
