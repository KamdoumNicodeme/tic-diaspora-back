package com.ticdiaspora.infrastructure.adapter.out.persistence;

import com.ticdiaspora.application.port.out.AbsenceRequestRepositoryPort;
import com.ticdiaspora.domain.model.*;
import com.ticdiaspora.domain.model.enums.*;
import com.ticdiaspora.infrastructure.adapter.out.persistence.mapper.AbsenceRequestPersistenceMapper;
import com.ticdiaspora.infrastructure.adapter.out.persistence.repository.AbsenceRequestJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.*;
import java.util.*;

@Repository
public class AbsenceRequestPersistenceAdapter implements AbsenceRequestRepositoryPort {

    private final AbsenceRequestJpaRepository repository;
    private final AbsenceRequestPersistenceMapper mapper;

    public AbsenceRequestPersistenceAdapter(AbsenceRequestJpaRepository repository, AbsenceRequestPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Page<AbsenceRequest> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDomain);
    }
    @Override
    public Optional<AbsenceRequest> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }
    @Override
    public AbsenceRequest save(AbsenceRequest absenceRequest) {
        return mapper.toDomain(repository.save(mapper.toEntity(absenceRequest)));
    }
    @Override
    public List<AbsenceRequest> findAllByMemberIdOrderByRequestedAtDesc(UUID memberId) {
        return mapper.toDomainList(repository.findAllByMemberIdOrderByRequestedAtDesc(memberId));
    }
    @Override
    public List<AbsenceRequest> findAllByMeetingId(UUID meetingId) {
        return mapper.toDomainList(repository.findAllByMeetingId(meetingId));
    }
}
