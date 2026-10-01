package com.ticdiaspora.infrastructure.adapter.out.persistence.repository;

import com.ticdiaspora.infrastructure.adapter.out.persistence.entity.*;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AbsenceRequestJpaRepository extends JpaRepository<AbsenceRequestEntity, UUID> {
    List<AbsenceRequestEntity> findAllByMemberIdOrderByRequestedAtDesc(UUID memberId);

    List<AbsenceRequestEntity> findAllByMeetingId(UUID meetingId);
}
