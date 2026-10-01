package com.ticdiaspora.infrastructure.adapter.out.persistence.repository;

import com.ticdiaspora.infrastructure.adapter.out.persistence.entity.*;

import com.ticdiaspora.domain.model.enums.ContributionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContributionJpaRepository extends JpaRepository<ContributionEntity, UUID> {
    List<ContributionEntity> findAllByCycleId(UUID cycleId);

    List<ContributionEntity> findAllByMemberId(UUID memberId);

    Optional<ContributionEntity> findByCycleIdAndMemberId(UUID cycleId, UUID memberId);

    long countByCycleIdAndStatusNot(UUID cycleId, ContributionStatus status);
}
