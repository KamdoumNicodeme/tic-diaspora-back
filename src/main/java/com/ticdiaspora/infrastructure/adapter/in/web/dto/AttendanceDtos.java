package com.ticdiaspora.infrastructure.adapter.in.web.dto;

import com.ticdiaspora.domain.model.enums.AttendanceStatus;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public final class AttendanceDtos {
    private AttendanceDtos() {
    }

    public record CheckInRequest(@NotNull UUID meetingId, @NotNull UUID memberId, @NotNull LocalDateTime arrivalAt) {
    }

    public record AbsenceRecordRequest(@NotNull UUID meetingId, @NotNull UUID memberId, boolean authorized, UUID absenceRequestId) {
    }

    public record AttendanceResponse(
            UUID id,
            UUID meetingId,
            UUID memberId,
            AttendanceStatus status,
            LocalDateTime expectedStartAt,
            LocalDateTime arrivalAt,
            int delayMinutes,
            UUID penaltyId,
            UUID absenceRequestId,
            UUID recordedBy,
            Instant createdAt
    ) {
    }
}
