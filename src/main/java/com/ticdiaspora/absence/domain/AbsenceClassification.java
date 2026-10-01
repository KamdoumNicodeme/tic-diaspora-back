package com.ticdiaspora.absence.domain;

import com.ticdiaspora.shared.domain.enums.AbsenceRequestStatus;

public record AbsenceClassification(
        AbsenceRequestStatus status,
        long hoursBeforeMeeting,
        boolean requiresManualValidation,
        boolean requiresExceptionalApproval
) {
}
