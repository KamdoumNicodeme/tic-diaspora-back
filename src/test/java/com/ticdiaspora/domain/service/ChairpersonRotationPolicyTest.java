package com.ticdiaspora.domain.service;

import com.ticdiaspora.domain.model.enums.MemberStatus;
import com.ticdiaspora.domain.model.ChairpersonCandidate;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ChairpersonRotationPolicyTest {

    private final ChairpersonRotationPolicy policy = new ChairpersonRotationPolicy();

    @Test
    void prioritizesMembersWhoNeverChaired() {
        UUID experienced = UUID.randomUUID();
        UUID neverChaired = UUID.randomUUID();

        UUID selected = policy.proposeNextChairperson(List.of(
                new ChairpersonCandidate(experienced, MemberStatus.ACTIVE, 2, Instant.parse("2025-01-01T00:00:00Z"), LocalDate.parse("2020-01-01")),
                new ChairpersonCandidate(neverChaired, MemberStatus.ACTIVE, 0, null, LocalDate.parse("2024-01-01"))
        ));

        assertThat(selected).isEqualTo(neverChaired);
    }

    @Test
    void excludesSuspendedMembersAndChoosesLeastFrequentOldestAnimation() {
        UUID suspended = UUID.randomUUID();
        UUID activeRecent = UUID.randomUUID();
        UUID activeOld = UUID.randomUUID();

        UUID selected = policy.proposeNextChairperson(List.of(
                new ChairpersonCandidate(suspended, MemberStatus.SUSPENDED, 0, null, LocalDate.parse("2020-01-01")),
                new ChairpersonCandidate(activeRecent, MemberStatus.ACTIVE, 1, Instant.parse("2025-06-01T00:00:00Z"), LocalDate.parse("2020-01-01")),
                new ChairpersonCandidate(activeOld, MemberStatus.ACTIVE, 1, Instant.parse("2024-06-01T00:00:00Z"), LocalDate.parse("2021-01-01"))
        ));

        assertThat(selected).isEqualTo(activeOld);
    }
}
