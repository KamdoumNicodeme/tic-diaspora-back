package com.ticdiaspora.meeting.infrastructure.persistence;

import com.ticdiaspora.shared.domain.enums.MeetingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MeetingJpaRepository extends JpaRepository<MeetingEntity, UUID> {
    Optional<MeetingEntity> findFirstByMeetingDateGreaterThanEqualAndStatusOrderByMeetingDateAsc(LocalDate date, MeetingStatus status);

    boolean existsByMeetingDate(LocalDate meetingDate);

    List<MeetingEntity> findAllByMeetingDateBetweenOrderByMeetingDateAsc(LocalDate from, LocalDate to);
}
