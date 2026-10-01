package com.ticdiaspora.absence.domain;

import com.ticdiaspora.shared.domain.enums.AbsenceRequestStatus;

import java.time.Duration;
import java.time.Instant;

public class AbsenceRules {

    public AbsenceClassification classify(Instant requestedAt, Instant meetingStartAt) {
        long hoursBeforeMeeting = Duration.between(requestedAt, meetingStartAt).toHours();
        if (hoursBeforeMeeting >= 48) {
            return new AbsenceClassification(AbsenceRequestStatus.AUTO_APPROVED, hoursBeforeMeeting, false, false);
        }
        if (hoursBeforeMeeting >= 24) {
            return new AbsenceClassification(AbsenceRequestStatus.PENDING, hoursBeforeMeeting, true, false);
        }
        return new AbsenceClassification(AbsenceRequestStatus.LATE_REQUEST, hoursBeforeMeeting, true, true);
    }

    public boolean canApproveLateRequest(boolean exceptionalApproval, String validationComment) {
        return exceptionalApproval && validationComment != null && !validationComment.isBlank();
    }
}
