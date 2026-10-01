package com.ticdiaspora.beneficiary.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BeneficiaryConfirmationJpaRepository extends JpaRepository<BeneficiaryConfirmationEntity, UUID> {
    Optional<BeneficiaryConfirmationEntity> findByCycleId(UUID cycleId);
}
