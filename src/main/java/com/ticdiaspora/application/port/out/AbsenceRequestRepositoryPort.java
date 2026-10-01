package com.ticdiaspora.application.port.out;

import com.ticdiaspora.domain.model.*;
import com.ticdiaspora.domain.model.enums.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.*;
import java.util.*;

public interface AbsenceRequestRepositoryPort {
    Page<AbsenceRequest> findAll(Pageable pageable);

    Optional<AbsenceRequest> findById(UUID id);

    AbsenceRequest save(AbsenceRequest absenceRequest);

    List<AbsenceRequest> findAllByMemberIdOrderByRequestedAtDesc(UUID memberId);

    List<AbsenceRequest> findAllByMeetingId(UUID meetingId);
}
