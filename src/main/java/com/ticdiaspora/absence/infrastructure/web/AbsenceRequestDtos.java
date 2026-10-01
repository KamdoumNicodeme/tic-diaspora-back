package com.ticdiaspora.absence.infrastructure.web;

import com.ticdiaspora.shared.domain.enums.AbsenceRequestStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
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
            UUID meetingId,
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
