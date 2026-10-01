package com.ticdiaspora.domain.service;

import com.ticdiaspora.domain.model.*;

import com.ticdiaspora.domain.model.enums.PenaltyType;
import com.ticdiaspora.domain.exception.ForbiddenOperationException;

public class PenaltyRules {

    public long amountFor(PenaltyType type) {
        return switch (type) {
            case LATE_10 -> 1_000;
            case LATE_20 -> 2_000;
            case ABSENCE, ABSENT_FROM_DELAY -> 5_000;
        };
    }

    public void verifyCancellationReason(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new ForbiddenOperationException("Une annulation de pénalité exige une justification");
        }
    }
}
