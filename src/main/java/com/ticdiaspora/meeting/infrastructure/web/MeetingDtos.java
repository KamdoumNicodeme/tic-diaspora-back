package com.ticdiaspora.meeting.infrastructure.web;

import com.ticdiaspora.shared.domain.enums.MeetingStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public final class MeetingDtos {
    private MeetingDtos() {
    }

    public record MeetingRequest(
            @NotBlank String title,
            @NotNull LocalDate meetingDate,
            @NotNull LocalTime plannedStartTime,
            @NotNull LocalTime plannedEndTime,
            String onlineLink,
            UUID chairpersonId,
            String notes,
            String decisionsSummary,
            String projectsSummary
    ) {
    }

    public record CompleteMeetingRequest(
            String notes,
            String decisionsSummary,
            String projectsSummary
    ) {
    }

    public record ChangeChairpersonRequest(@NotNull UUID chairpersonId, String reason) {
    }

    public record MeetingResponse(
            UUID id,
            String title,
            LocalDate meetingDate,
            LocalTime plannedStartTime,
            LocalTime plannedEndTime,
            String onlineLink,
            MeetingStatus status,
            UUID chairpersonId,
            String notes,
            String decisionsSummary,
            String projectsSummary,
            Instant createdAt,
            Instant startedAt,
            Instant completedAt,
            Instant cancelledAt
    ) {
    }
}
