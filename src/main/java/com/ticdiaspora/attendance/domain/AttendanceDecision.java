package com.ticdiaspora.attendance.domain;

import com.ticdiaspora.shared.domain.enums.AttendanceStatus;
import com.ticdiaspora.shared.domain.enums.PenaltyType;

import java.util.Optional;

public record AttendanceDecision(
        AttendanceStatus status,
        int delayMinutes,
        long penaltyAmount,
        PenaltyType penaltyType
) {
    public Optional<PenaltyType> optionalPenaltyType() {
        return Optional.ofNullable(penaltyType);
    }
}
