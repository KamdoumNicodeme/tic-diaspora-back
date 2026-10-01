package com.ticdiaspora.domain.service;

import com.ticdiaspora.domain.model.enums.AbsenceRequestStatus;
import com.ticdiaspora.domain.model.AbsenceClassification;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class AbsenceRulesTest {

    private final AbsenceRules rules = new AbsenceRules();

    @Test
    void requestAtLeastFortyEightHoursBeforeMeetingIsAutoApproved() {
        Instant requestedAt = Instant.parse("2026-01-02T15:00:00Z");
        Instant meetingAt = requestedAt.plus(48, ChronoUnit.HOURS);

        AbsenceClassification classification = rules.classify(requestedAt, meetingAt);

        assertThat(classification.status()).isEqualTo(AbsenceRequestStatus.AUTO_APPROVED);
        assertThat(classification.requiresManualValidation()).isFalse();
    }

    @Test
    void requestBetweenTwentyFourAndFortyEightHoursNeedsValidation() {
        Instant requestedAt = Instant.parse("2026-01-03T00:00:00Z");
        Instant meetingAt = requestedAt.plus(36, ChronoUnit.HOURS);

        AbsenceClassification classification = rules.classify(requestedAt, meetingAt);

        assertThat(classification.status()).isEqualTo(AbsenceRequestStatus.PENDING);
        assertThat(classification.requiresManualValidation()).isTrue();
    }

    @Test
    void requestLessThanTwentyFourHoursIsLate() {
        Instant requestedAt = Instant.parse("2026-01-04T00:00:00Z");
        Instant meetingAt = requestedAt.plus(23, ChronoUnit.HOURS);

        AbsenceClassification classification = rules.classify(requestedAt, meetingAt);

        assertThat(classification.status()).isEqualTo(AbsenceRequestStatus.LATE_REQUEST);
        assertThat(classification.requiresExceptionalApproval()).isTrue();
        assertThat(rules.canApproveLateRequest(true, "Cas exceptionnel validé")).isTrue();
    }
}
