package com.ticdiaspora.infrastructure.adapter.out.persistence.repository;

import com.ticdiaspora.infrastructure.adapter.out.persistence.entity.*;

import com.ticdiaspora.domain.model.enums.DecisionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface DecisionJpaRepository extends JpaRepository<DecisionEntity, UUID> {
    List<DecisionEntity> findAllByMeetingId(UUID meetingId);

    List<DecisionEntity> findAllByProjectId(UUID projectId);

    long countByProjectId(UUID projectId);

    long countByProjectIdAndStatusIn(UUID projectId, Collection<DecisionStatus> statuses);

    long countByStatusIn(Collection<DecisionStatus> statuses);

    List<DecisionEntity> findTop5ByStatusInOrderByDueDateAsc(Collection<DecisionStatus> statuses);
}
