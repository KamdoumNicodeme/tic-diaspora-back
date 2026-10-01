package com.ticdiaspora.infrastructure.adapter.out.persistence.repository;

import com.ticdiaspora.infrastructure.adapter.out.persistence.entity.*;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContributionPaymentJpaRepository extends JpaRepository<ContributionPaymentEntity, UUID> {
    List<ContributionPaymentEntity> findAllByContributionId(UUID contributionId);

    Optional<ContributionPaymentEntity> findFirstByContributionIdOrderByPaidAtDesc(UUID contributionId);

    Optional<ContributionPaymentEntity> findByProofUrl(String proofUrl);
}
