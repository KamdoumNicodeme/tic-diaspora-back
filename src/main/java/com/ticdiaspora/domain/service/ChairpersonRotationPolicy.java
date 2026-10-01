package com.ticdiaspora.domain.service;

import com.ticdiaspora.domain.model.*;

import com.ticdiaspora.domain.model.enums.MemberStatus;
import com.ticdiaspora.domain.exception.BusinessException;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

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
