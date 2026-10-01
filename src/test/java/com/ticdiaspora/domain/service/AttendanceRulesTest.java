package com.ticdiaspora.domain.service;

import com.ticdiaspora.domain.model.enums.AttendanceStatus;
import com.ticdiaspora.domain.model.AttendanceDecision;
import com.ticdiaspora.domain.model.enums.PenaltyType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AttendanceRulesTest {

    private final AttendanceRules rules = new AttendanceRules();
    private final LocalDateTime start = LocalDateTime.of(2026, 1, 4, 15, 0);

    @Test
    void delayUnderTenMinutesIsOnTimeWithoutPenalty() {
        AttendanceDecision decision = rules.evaluateArrival(start, start.plusMinutes(9));

        assertThat(decision.status()).isEqualTo(AttendanceStatus.PRESENT_ON_TIME);
        assertThat(decision.penaltyAmount()).isZero();
    }

    @Test
    void tenToNineteenMinutesDelayCreatesLate10Penalty() {
        AttendanceDecision decision = rules.evaluateArrival(start, start.plusMinutes(10));

        assertThat(decision.status()).isEqualTo(AttendanceStatus.LATE_10);
        assertThat(decision.penaltyAmount()).isEqualTo(1_000);
        assertThat(decision.penaltyType()).isEqualTo(PenaltyType.LATE_10);
    }

    @Test
    void twentyToTwentyNineMinutesDelayCreatesLate20Penalty() {
        AttendanceDecision decision = rules.evaluateArrival(start, start.plusMinutes(29));

        assertThat(decision.status()).isEqualTo(AttendanceStatus.LATE_20);
        assertThat(decision.penaltyAmount()).isEqualTo(2_000);
        assertThat(decision.penaltyType()).isEqualTo(PenaltyType.LATE_20);
    }

    @Test
    void thirtyMinutesDelayBecomesAbsenceFromDelay() {
        AttendanceDecision decision = rules.evaluateArrival(start, start.plusMinutes(30));

        assertThat(decision.status()).isEqualTo(AttendanceStatus.ABSENT_FROM_DELAY);
        assertThat(decision.penaltyAmount()).isEqualTo(5_000);
        assertThat(rules.contributesToUnauthorizedAbsenceThreshold(decision.status())).isTrue();
    }
}
