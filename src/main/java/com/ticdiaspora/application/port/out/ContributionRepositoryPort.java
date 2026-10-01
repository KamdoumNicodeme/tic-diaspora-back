package com.ticdiaspora.application.port.out;

import com.ticdiaspora.domain.model.*;
import com.ticdiaspora.domain.model.enums.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.*;
import java.util.*;

public interface ContributionRepositoryPort {
    Page<Contribution> findAll(Pageable pageable);

    List<Contribution> findAll();

    Optional<Contribution> findById(UUID id);

    Contribution save(Contribution contribution);

    List<Contribution> findAllByCycleId(UUID cycleId);

    List<Contribution> findAllByMemberId(UUID memberId);

    Optional<Contribution> findByCycleIdAndMemberId(UUID cycleId, UUID memberId);

    long countByCycleIdAndStatusNot(UUID cycleId, ContributionStatus status);
}
