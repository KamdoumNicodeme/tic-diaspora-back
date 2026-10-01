package com.ticdiaspora.penalty.domain;

import com.ticdiaspora.shared.domain.enums.PenaltyType;
import com.ticdiaspora.shared.domain.exception.ForbiddenOperationException;
import org.springframework.stereotype.Component;

@Component
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
