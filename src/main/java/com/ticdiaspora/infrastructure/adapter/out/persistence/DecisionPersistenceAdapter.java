package com.ticdiaspora.infrastructure.adapter.out.persistence;

import com.ticdiaspora.application.port.out.DecisionRepositoryPort;
import com.ticdiaspora.domain.model.*;
import com.ticdiaspora.domain.model.enums.*;
import com.ticdiaspora.infrastructure.adapter.out.persistence.mapper.DecisionPersistenceMapper;
import com.ticdiaspora.infrastructure.adapter.out.persistence.repository.DecisionJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.*;
import java.util.*;

@Repository
public class DecisionPersistenceAdapter implements DecisionRepositoryPort {

    private final DecisionJpaRepository repository;
    private final DecisionPersistenceMapper mapper;

    public DecisionPersistenceAdapter(DecisionJpaRepository repository, DecisionPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Page<Decision> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDomain);
    }
    @Override
    public Optional<Decision> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }
    @Override
    public Decision save(Decision decision) {
        return mapper.toDomain(repository.save(mapper.toEntity(decision)));
    }
    @Override
    public List<Decision> findAllByMeetingId(UUID meetingId) {
        return mapper.toDomainList(repository.findAllByMeetingId(meetingId));
    }
    @Override
    public List<Decision> findAllByProjectId(UUID projectId) {
        return mapper.toDomainList(repository.findAllByProjectId(projectId));
    }
    @Override
    public long countByProjectId(UUID projectId) {
        return repository.countByProjectId(projectId);
    }
    @Override
    public long countByProjectIdAndStatusIn(UUID projectId, Collection<DecisionStatus> statuses) {
        return repository.countByProjectIdAndStatusIn(projectId, statuses);
    }
    @Override
    public long countByStatusIn(Collection<DecisionStatus> statuses) {
        return repository.countByStatusIn(statuses);
    }
    @Override
    public List<Decision> findTop5ByStatusInOrderByDueDateAsc(Collection<DecisionStatus> statuses) {
        return mapper.toDomainList(repository.findTop5ByStatusInOrderByDueDateAsc(statuses));
    }
}
