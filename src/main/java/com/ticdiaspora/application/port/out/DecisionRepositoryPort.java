package com.ticdiaspora.application.port.out;

import com.ticdiaspora.domain.model.*;
import com.ticdiaspora.domain.model.enums.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.*;
import java.util.*;

public interface DecisionRepositoryPort {
    Page<Decision> findAll(Pageable pageable);

    Optional<Decision> findById(UUID id);

    Decision save(Decision decision);

    List<Decision> findAllByMeetingId(UUID meetingId);

    List<Decision> findAllByProjectId(UUID projectId);

    long countByProjectId(UUID projectId);

    long countByProjectIdAndStatusIn(UUID projectId, Collection<DecisionStatus> statuses);

    long countByStatusIn(Collection<DecisionStatus> statuses);

    List<Decision> findTop5ByStatusInOrderByDueDateAsc(Collection<DecisionStatus> statuses);
}
