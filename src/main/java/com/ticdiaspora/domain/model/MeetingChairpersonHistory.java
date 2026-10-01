package com.ticdiaspora.domain.model;

import java.time.Instant;
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
public class MeetingChairpersonHistory {

    private UUID id;
    private UUID meetingId;
    private UUID memberId;
    private String assignmentType;
    private UUID assignedBy;
    private String reason;
    private Instant assignedAt;
    private Instant completedAt;
}
