package com.ticdiaspora.infrastructure.adapter.in.web.dto;

import com.ticdiaspora.domain.model.enums.ApplicationRole;
import com.ticdiaspora.domain.model.enums.ContributionType;
import com.ticdiaspora.domain.model.enums.MemberStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public final class MemberDtos {
    private MemberDtos() {
    }

    public record MemberRequest(
            @NotBlank String firstName,
            @NotBlank String lastName,
            @Email @NotBlank String email,
            String phone,
            String country,
            String city,
            String fullAddress,
            @NotNull LocalDate joinedAt,
            @NotNull ApplicationRole role,
            @NotNull ContributionType contributionType,
            String photoUrl,
            String emergencyContactName,
            String emergencyContactPhone,
            String orangeMoneyNumber,
            String orangeMoneyAccountName,
            String mtnMoneyNumber,
            String mtnMoneyAccountName,
            String temporaryPassword
    ) {
    }

    public record ProfileUpdateRequest(
            @NotBlank String firstName,
            @NotBlank String lastName,
            String phone,
            String country,
            String city,
            String fullAddress,
            String photoUrl,
            String emergencyContactName,
            String emergencyContactPhone,
            String orangeMoneyNumber,
            String orangeMoneyAccountName,
            String mtnMoneyNumber,
            String mtnMoneyAccountName
    ) {
    }

    public record MemberResponse(
            UUID id,
            String firstName,
            String lastName,
            String email,
            String phone,
            String country,
            String city,
            String fullAddress,
            LocalDate joinedAt,
            MemberStatus status,
            ApplicationRole role,
            ContributionType contributionType,
            String photoUrl,
            String emergencyContactName,
            String emergencyContactPhone,
            String orangeMoneyNumber,
            String orangeMoneyAccountName,
            String mtnMoneyNumber,
            String mtnMoneyAccountName,
            int totalMeetingsChaired,
            Instant lastChairedAt,
            int annualAuthorizedAbsences,
            int annualUnauthorizedAbsences,
            long totalPenaltiesAmount,
            long unpaidPenaltiesAmount,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}
