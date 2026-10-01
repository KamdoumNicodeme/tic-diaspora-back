package com.ticdiaspora.infrastructure.adapter.out.persistence;

import com.ticdiaspora.application.port.out.MeetingRepositoryPort;
import com.ticdiaspora.domain.model.*;
import com.ticdiaspora.domain.model.enums.*;
import com.ticdiaspora.infrastructure.adapter.out.persistence.mapper.MeetingPersistenceMapper;
import com.ticdiaspora.infrastructure.adapter.out.persistence.repository.MeetingJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.*;
import java.util.*;

@Repository
public class MeetingPersistenceAdapter implements MeetingRepositoryPort {

    private final MeetingJpaRepository repository;
    private final MeetingPersistenceMapper mapper;

    public MeetingPersistenceAdapter(MeetingJpaRepository repository, MeetingPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Page<Meeting> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDomain);
    }
    @Override
    public List<Meeting> findAll() {
        return mapper.toDomainList(repository.findAll());
    }
    @Override
    public Optional<Meeting> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }
    @Override
    public Meeting save(Meeting meeting) {
        return mapper.toDomain(repository.save(mapper.toEntity(meeting)));
    }
    @Override
    public Optional<Meeting> findFirstByMeetingDateGreaterThanEqualAndStatusOrderByMeetingDateAsc(LocalDate date, MeetingStatus status) {
        return repository.findFirstByMeetingDateGreaterThanEqualAndStatusOrderByMeetingDateAsc(date, status).map(mapper::toDomain);
    }
    @Override
    public boolean existsByMeetingDate(LocalDate meetingDate) {
        return repository.existsByMeetingDate(meetingDate);
    }
    @Override
    public List<Meeting> findAllByMeetingDateBetweenOrderByMeetingDateAsc(LocalDate from, LocalDate to) {
        return mapper.toDomainList(repository.findAllByMeetingDateBetweenOrderByMeetingDateAsc(from, to));
    }
}
