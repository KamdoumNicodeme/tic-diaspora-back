package com.ticdiaspora.infrastructure.adapter.in.web.dto;

import com.ticdiaspora.domain.model.enums.TontineCycleStatus;
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
            UUID secondaryBeneficiaryId,
            String comment
    ) {
    }

    public record TontineCycleResponse(
            UUID id,
            int month,
            int year,
            UUID beneficiaryId,
            String beneficiaryFullName,
            String beneficiaryOrangeMoneyNumber,
            String beneficiaryOrangeMoneyAccountName,
            String beneficiaryMtnMoneyNumber,
            String beneficiaryMtnMoneyAccountName,
            long beneficiaryExpectedAmount,
            UUID secondaryBeneficiaryId,
            String secondaryBeneficiaryFullName,
            String secondaryBeneficiaryOrangeMoneyNumber,
            String secondaryBeneficiaryOrangeMoneyAccountName,
            String secondaryBeneficiaryMtnMoneyNumber,
            String secondaryBeneficiaryMtnMoneyAccountName,
            long secondaryBeneficiaryExpectedAmount,
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

    public record ChangeBeneficiaryRequest(@NotNull UUID beneficiaryId, UUID secondaryBeneficiaryId,
                                           boolean unanimousAgreement, String reason) {
    }
}
