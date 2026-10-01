package com.ticdiaspora.infrastructure.adapter.out.persistence.repository;

import com.ticdiaspora.infrastructure.adapter.out.persistence.entity.*;

import com.ticdiaspora.domain.model.enums.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttendanceRecordJpaRepository extends JpaRepository<AttendanceRecordEntity, UUID> {
    Optional<AttendanceRecordEntity> findByMeetingIdAndMemberId(UUID meetingId, UUID memberId);

    List<AttendanceRecordEntity> findAllByMeetingId(UUID meetingId);

    List<AttendanceRecordEntity> findAllByMemberIdOrderByExpectedStartAtDesc(UUID memberId);

    @Query("""
            select count(a) from AttendanceRecordEntity a
            where a.memberId = :memberId
              and a.status in :statuses
              and a.expectedStartAt >= :from
              and a.expectedStartAt < :to
            """)
    long countUnauthorizedAbsencesInPeriod(
            @Param("memberId") UUID memberId,
            @Param("statuses") List<AttendanceStatus> statuses,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}
