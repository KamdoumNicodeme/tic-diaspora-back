package com.ticdiaspora.infrastructure.adapter.out.persistence.repository;

import com.ticdiaspora.infrastructure.adapter.out.persistence.entity.*;

import com.ticdiaspora.domain.model.enums.PenaltyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PenaltyJpaRepository extends JpaRepository<PenaltyEntity, UUID> {
    Page<PenaltyEntity> findAllByStatus(PenaltyStatus status, Pageable pageable);

    List<PenaltyEntity> findAllByMemberIdOrderByCreatedAtDesc(UUID memberId);

    List<PenaltyEntity> findAllByMeetingId(UUID meetingId);

    @Query("select coalesce(sum(p.amount), 0) from PenaltyEntity p where p.status = :status")
    long sumAmountByStatus(@Param("status") PenaltyStatus status);

    long countByStatus(PenaltyStatus status);
}
