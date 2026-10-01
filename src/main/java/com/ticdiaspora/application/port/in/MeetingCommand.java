package com.ticdiaspora.application.port.in;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record MeetingCommand(
        String title,
        LocalDate meetingDate,
        LocalTime plannedStartTime,
        LocalTime plannedEndTime,
        String onlineLink,
        UUID chairpersonId,
        String notes,
        String decisionsSummary,
        String projectsSummary
) {
}
