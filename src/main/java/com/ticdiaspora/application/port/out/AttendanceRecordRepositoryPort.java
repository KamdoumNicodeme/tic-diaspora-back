package com.ticdiaspora.application.port.out;

import com.ticdiaspora.domain.model.*;
import com.ticdiaspora.domain.model.enums.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.*;
import java.util.*;

public interface AttendanceRecordRepositoryPort {
    Page<AttendanceRecord> findAll(Pageable pageable);

    List<AttendanceRecord> findAll();

    Optional<AttendanceRecord> findById(UUID id);

    AttendanceRecord save(AttendanceRecord attendanceRecord);

    Optional<AttendanceRecord> findByMeetingIdAndMemberId(UUID meetingId, UUID memberId);

    List<AttendanceRecord> findAllByMeetingId(UUID meetingId);

    List<AttendanceRecord> findAllByMemberIdOrderByExpectedStartAtDesc(UUID memberId);

    long countUnauthorizedAbsencesInPeriod(UUID memberId, List<AttendanceStatus> statuses, LocalDateTime from, LocalDateTime to);
}
