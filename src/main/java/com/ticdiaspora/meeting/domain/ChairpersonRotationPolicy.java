package com.ticdiaspora.meeting.domain;

import com.ticdiaspora.shared.domain.enums.MemberStatus;
import com.ticdiaspora.shared.domain.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Component
public class ChairpersonRotationPolicy {

    public UUID proposeNextChairperson(List<ChairpersonCandidate> candidates) {
        return candidates.stream()
                .filter(candidate -> candidate.status() == MemberStatus.ACTIVE)
                .min(Comparator
                        .comparingInt(ChairpersonCandidate::totalMeetingsChaired)
                        .thenComparing(candidate -> candidate.lastChairedAt() == null ? Instant.EPOCH : candidate.lastChairedAt())
                        .thenComparing(ChairpersonCandidate::joinedAt)
                        .thenComparing(ChairpersonCandidate::memberId))
                .map(ChairpersonCandidate::memberId)
                .orElseThrow(() -> new BusinessException("NO_ELIGIBLE_CHAIRPERSON", "Aucun membre actif éligible pour animer la réunion"));
    }
}
