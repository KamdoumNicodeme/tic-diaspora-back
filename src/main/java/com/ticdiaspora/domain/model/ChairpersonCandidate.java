package com.ticdiaspora.domain.model;

import com.ticdiaspora.domain.model.enums.MemberStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ChairpersonCandidate(
        UUID memberId,
        MemberStatus status,
        int totalMeetingsChaired,
        Instant lastChairedAt,
        LocalDate joinedAt
) {
}
