package com.ticdiaspora.meeting.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MeetingChairpersonHistoryJpaRepository extends JpaRepository<MeetingChairpersonHistoryEntity, UUID> {
    List<MeetingChairpersonHistoryEntity> findAllByMemberIdOrderByAssignedAtDesc(UUID memberId);
}
