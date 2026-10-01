package com.ticdiaspora.penalty.domain;

import com.ticdiaspora.shared.domain.enums.PenaltyType;
import com.ticdiaspora.shared.domain.exception.ForbiddenOperationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PenaltyRulesTest {

    private final PenaltyRules rules = new PenaltyRules();

    @Test
    void returnsConfiguredPenaltyAmounts() {
        assertThat(rules.amountFor(PenaltyType.LATE_10)).isEqualTo(1_000);
        assertThat(rules.amountFor(PenaltyType.LATE_20)).isEqualTo(2_000);
        assertThat(rules.amountFor(PenaltyType.ABSENCE)).isEqualTo(5_000);
        assertThat(rules.amountFor(PenaltyType.ABSENT_FROM_DELAY)).isEqualTo(5_000);
    }

    @Test
    void requiresReasonToCancelPenalty() {
        assertThatThrownBy(() -> rules.verifyCancellationReason(" "))
                .isInstanceOf(ForbiddenOperationException.class);
    }
}
