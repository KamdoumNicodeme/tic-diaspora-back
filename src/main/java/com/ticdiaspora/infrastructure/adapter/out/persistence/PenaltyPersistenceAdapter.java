package com.ticdiaspora.infrastructure.adapter.out.persistence;

import com.ticdiaspora.application.port.out.PenaltyRepositoryPort;
import com.ticdiaspora.domain.model.*;
import com.ticdiaspora.domain.model.enums.*;
import com.ticdiaspora.infrastructure.adapter.out.persistence.mapper.PenaltyPersistenceMapper;
import com.ticdiaspora.infrastructure.adapter.out.persistence.repository.PenaltyJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.*;
import java.util.*;

@Repository
public class PenaltyPersistenceAdapter implements PenaltyRepositoryPort {

    private final PenaltyJpaRepository repository;
    private final PenaltyPersistenceMapper mapper;

    public PenaltyPersistenceAdapter(PenaltyJpaRepository repository, PenaltyPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Page<Penalty> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDomain);
    }
    @Override
    public Page<Penalty> findAllByStatus(PenaltyStatus status, Pageable pageable) {
        return repository.findAllByStatus(status, pageable).map(mapper::toDomain);
    }
    @Override
    public Optional<Penalty> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }
    @Override
    public Penalty save(Penalty penalty) {
        return mapper.toDomain(repository.save(mapper.toEntity(penalty)));
    }
    @Override
    public List<Penalty> findAllByMemberIdOrderByCreatedAtDesc(UUID memberId) {
        return mapper.toDomainList(repository.findAllByMemberIdOrderByCreatedAtDesc(memberId));
    }
    @Override
    public List<Penalty> findAllByMeetingId(UUID meetingId) {
        return mapper.toDomainList(repository.findAllByMeetingId(meetingId));
    }
    @Override
    public long sumAmountByStatus(PenaltyStatus status) {
        return repository.sumAmountByStatus(status);
    }
    @Override
    public long countByStatus(PenaltyStatus status) {
        return repository.countByStatus(status);
    }
    @Override
    public long count() {
        return repository.count();
    }
}
