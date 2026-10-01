package com.ticdiaspora.domain.model;

import com.ticdiaspora.domain.model.enums.AttendanceStatus;
import java.time.Instant;
import java.time.LocalDateTime;
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
public class AttendanceRecord {

    private UUID id;
    private UUID meetingId;
    private UUID memberId;
    private AttendanceStatus status;
    private LocalDateTime expectedStartAt;
    private LocalDateTime arrivalAt;
    private int delayMinutes;
    private UUID penaltyId;
    private UUID absenceRequestId;
    private UUID recordedBy;
    private Instant createdAt;
}
