package com.ticdiaspora.attendance.domain;

import com.ticdiaspora.shared.domain.enums.AttendanceStatus;
import com.ticdiaspora.shared.domain.enums.PenaltyType;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class AttendanceRules {

    public AttendanceDecision evaluateArrival(LocalDateTime expectedStartAt, LocalDateTime arrivalAt) {
        int delayMinutes = Math.max(0, (int) Duration.between(expectedStartAt, arrivalAt).toMinutes());
        if (delayMinutes < 10) {
            return new AttendanceDecision(AttendanceStatus.PRESENT_ON_TIME, delayMinutes, 0, null);
        }
        if (delayMinutes < 20) {
            return new AttendanceDecision(AttendanceStatus.LATE_10, delayMinutes, 1_000, PenaltyType.LATE_10);
        }
        if (delayMinutes < 30) {
            return new AttendanceDecision(AttendanceStatus.LATE_20, delayMinutes, 2_000, PenaltyType.LATE_20);
        }
        return new AttendanceDecision(AttendanceStatus.ABSENT_FROM_DELAY, delayMinutes, 5_000, PenaltyType.ABSENT_FROM_DELAY);
    }

    public AttendanceDecision unauthorizedAbsence() {
        return new AttendanceDecision(AttendanceStatus.ABSENT_UNAUTHORIZED, 0, 5_000, PenaltyType.ABSENCE);
    }

    public boolean contributesToUnauthorizedAbsenceThreshold(AttendanceStatus status) {
        return status == AttendanceStatus.ABSENT_UNAUTHORIZED || status == AttendanceStatus.ABSENT_FROM_DELAY;
    }
}
