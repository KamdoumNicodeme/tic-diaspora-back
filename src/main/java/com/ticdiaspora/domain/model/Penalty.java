package com.ticdiaspora.domain.model;

import com.ticdiaspora.domain.model.enums.PenaltyStatus;
import com.ticdiaspora.domain.model.enums.PenaltyType;
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
public class Penalty {

    private UUID id;
    private UUID memberId;
    private UUID meetingId;
    private PenaltyType type;
    private long amount;
    @Builder.Default
    private PenaltyStatus status = PenaltyStatus.PENDING;
    private String reason;
    private Instant createdAt;
    private Instant paidAt;
    private UUID validatedBy;
    private String cancellationReason;
}
