package com.ticdiaspora.domain.model;

import com.ticdiaspora.domain.model.enums.AbsenceRequestStatus;
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
public class AbsenceRequest {

    private UUID id;
    private UUID memberId;
    private UUID meetingId;
    private String reason;
    private Instant requestedAt;
    private long hoursBeforeMeeting;
    private AbsenceRequestStatus status;
    private String validationComment;
    private UUID validatedBy;
    private Instant validatedAt;
    private boolean exceptionalApproval;
}
