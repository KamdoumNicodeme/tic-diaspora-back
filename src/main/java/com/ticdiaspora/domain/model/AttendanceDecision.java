package com.ticdiaspora.domain.model;

import com.ticdiaspora.domain.model.enums.AttendanceStatus;
import com.ticdiaspora.domain.model.enums.PenaltyType;

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
