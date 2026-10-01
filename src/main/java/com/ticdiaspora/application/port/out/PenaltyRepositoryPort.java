package com.ticdiaspora.application.port.out;

import com.ticdiaspora.domain.model.*;
import com.ticdiaspora.domain.model.enums.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.*;
import java.util.*;

public interface PenaltyRepositoryPort {
    Page<Penalty> findAll(Pageable pageable);

    Page<Penalty> findAllByStatus(PenaltyStatus status, Pageable pageable);

    Optional<Penalty> findById(UUID id);

    Penalty save(Penalty penalty);

    List<Penalty> findAllByMemberIdOrderByCreatedAtDesc(UUID memberId);

    List<Penalty> findAllByMeetingId(UUID meetingId);

    long sumAmountByStatus(PenaltyStatus status);

    long countByStatus(PenaltyStatus status);

    long count();
}
