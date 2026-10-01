package com.ticdiaspora.domain.model;

import com.ticdiaspora.domain.model.enums.MeetingStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Meeting {

    private UUID id;
    private String title;
    private LocalDate meetingDate;
    private LocalTime plannedStartTime;
    private LocalTime plannedEndTime;
    private String onlineLink;
    @Builder.Default
    private MeetingStatus status = MeetingStatus.PLANNED;
    private UUID chairpersonId;
    private String notes;
    private String decisionsSummary;
    private String projectsSummary;
    private Instant createdAt;
    private Instant startedAt;
    private Instant completedAt;
    private Instant cancelledAt;
}
