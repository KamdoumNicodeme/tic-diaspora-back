package com.ticdiaspora.domain.model;

import com.ticdiaspora.domain.model.enums.AbsenceRequestStatus;

public record AbsenceClassification(
        AbsenceRequestStatus status,
        long hoursBeforeMeeting,
        boolean requiresManualValidation,
        boolean requiresExceptionalApproval
) {
}
