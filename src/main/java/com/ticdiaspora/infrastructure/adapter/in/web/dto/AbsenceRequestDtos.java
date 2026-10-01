package com.ticdiaspora.infrastructure.adapter.in.web.dto;

import com.ticdiaspora.domain.model.enums.AbsenceRequestStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public final class AbsenceRequestDtos {
    private AbsenceRequestDtos() {
    }

    public record CreateAbsenceRequest(@NotNull UUID memberId, @NotNull UUID meetingId, @NotBlank String reason) {
    }

    public record ValidateAbsenceRequest(String validationComment, boolean exceptionalApproval) {
    }

    public record AbsenceRequestResponse(
            UUID id,
            UUID memberId,
            String memberFullName,
            UUID meetingId,
            String meetingTitle,
            LocalDate meetingDate,
            LocalTime plannedStartTime,
            String reason,
            Instant requestedAt,
            long hoursBeforeMeeting,
            AbsenceRequestStatus status,
            String validationComment,
            UUID validatedBy,
            Instant validatedAt,
            boolean exceptionalApproval
    ) {
    }
}
