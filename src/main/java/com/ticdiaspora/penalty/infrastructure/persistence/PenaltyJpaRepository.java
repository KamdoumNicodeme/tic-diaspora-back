package com.ticdiaspora.penalty.infrastructure.persistence;

import com.ticdiaspora.shared.domain.enums.PenaltyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PenaltyJpaRepository extends JpaRepository<PenaltyEntity, UUID> {
    List<PenaltyEntity> findAllByMemberIdOrderByCreatedAtDesc(UUID memberId);

    List<PenaltyEntity> findAllByMeetingId(UUID meetingId);

    @Query("select coalesce(sum(p.amount), 0) from PenaltyEntity p where p.status = :status")
    long sumAmountByStatus(@Param("status") PenaltyStatus status);
}
