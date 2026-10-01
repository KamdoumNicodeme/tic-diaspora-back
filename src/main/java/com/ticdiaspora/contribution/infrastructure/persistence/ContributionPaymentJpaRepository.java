package com.ticdiaspora.contribution.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ContributionPaymentJpaRepository extends JpaRepository<ContributionPaymentEntity, UUID> {
    List<ContributionPaymentEntity> findAllByContributionId(UUID contributionId);
}
