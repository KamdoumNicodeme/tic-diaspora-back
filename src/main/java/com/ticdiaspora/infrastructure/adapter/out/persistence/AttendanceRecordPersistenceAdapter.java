package com.ticdiaspora.infrastructure.adapter.out.persistence;

import com.ticdiaspora.application.port.out.AttendanceRecordRepositoryPort;
import com.ticdiaspora.domain.model.*;
import com.ticdiaspora.domain.model.enums.*;
import com.ticdiaspora.infrastructure.adapter.out.persistence.mapper.AttendanceRecordPersistenceMapper;
import com.ticdiaspora.infrastructure.adapter.out.persistence.repository.AttendanceRecordJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.*;
import java.util.*;

@Repository
public class AttendanceRecordPersistenceAdapter implements AttendanceRecordRepositoryPort {

    private final AttendanceRecordJpaRepository repository;
    private final AttendanceRecordPersistenceMapper mapper;

    public AttendanceRecordPersistenceAdapter(AttendanceRecordJpaRepository repository, AttendanceRecordPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Page<AttendanceRecord> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDomain);
    }

    @Override
    public List<AttendanceRecord> findAll() {
        return mapper.toDomainList(repository.findAll());
    }
    @Override
    public Optional<AttendanceRecord> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }
    @Override
    public AttendanceRecord save(AttendanceRecord attendanceRecord) {
        return mapper.toDomain(repository.save(mapper.toEntity(attendanceRecord)));
    }
    @Override
    public Optional<AttendanceRecord> findByMeetingIdAndMemberId(UUID meetingId, UUID memberId) {
        return repository.findByMeetingIdAndMemberId(meetingId, memberId).map(mapper::toDomain);
    }
    @Override
    public List<AttendanceRecord> findAllByMeetingId(UUID meetingId) {
        return mapper.toDomainList(repository.findAllByMeetingId(meetingId));
    }
    @Override
    public List<AttendanceRecord> findAllByMemberIdOrderByExpectedStartAtDesc(UUID memberId) {
        return mapper.toDomainList(repository.findAllByMemberIdOrderByExpectedStartAtDesc(memberId));
    }
    @Override
    public long countUnauthorizedAbsencesInPeriod(UUID memberId, List<AttendanceStatus> statuses, LocalDateTime from, LocalDateTime to) {
        return repository.countUnauthorizedAbsencesInPeriod(memberId, statuses, from, to);
    }
}
