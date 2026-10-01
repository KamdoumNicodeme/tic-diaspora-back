package com.ticdiaspora.infrastructure.adapter.out.persistence.repository;

import com.ticdiaspora.infrastructure.adapter.out.persistence.entity.*;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BeneficiaryConfirmationJpaRepository extends JpaRepository<BeneficiaryConfirmationEntity, UUID> {
    Optional<BeneficiaryConfirmationEntity> findByCycleId(UUID cycleId);
}
