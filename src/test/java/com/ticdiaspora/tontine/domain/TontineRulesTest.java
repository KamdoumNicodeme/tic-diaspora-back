package com.ticdiaspora.tontine.domain;

import com.ticdiaspora.shared.domain.enums.ContributionStatus;
import com.ticdiaspora.shared.domain.exception.ForbiddenOperationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TontineRulesTest {

    private final TontineRules rules = new TontineRules();

    @Test
    void calculatesExpectedAmountFromActiveMembers() {
        assertThat(rules.expectedAmount(List.of(50_000L, 100_000L, 50_000L))).isEqualTo(200_000);
    }

    @Test
    void detectsPartialPayment() {
        assertThat(rules.contributionStatus(100_000, 40_000)).isEqualTo(ContributionStatus.PARTIALLY_PAID);
        assertThat(rules.remainingAmount(100_000, 40_000)).isEqualTo(60_000);
    }

    @Test
    void refusesClosingWithMissingContributions() {
        assertThatThrownBy(() -> rules.verifyClosable(1, true))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void refusesClosingWithoutBeneficiaryConfirmation() {
        assertThatThrownBy(() -> rules.verifyClosable(0, false))
                .isInstanceOf(ForbiddenOperationException.class);
    }
}
