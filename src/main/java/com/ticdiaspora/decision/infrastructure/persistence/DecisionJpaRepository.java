package com.ticdiaspora.decision.infrastructure.persistence;

import com.ticdiaspora.shared.domain.enums.DecisionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface DecisionJpaRepository extends JpaRepository<DecisionEntity, UUID> {
    List<DecisionEntity> findAllByMeetingId(UUID meetingId);

    long countByStatusIn(Collection<DecisionStatus> statuses);

    List<DecisionEntity> findTop5ByStatusInOrderByDueDateAsc(Collection<DecisionStatus> statuses);
}
