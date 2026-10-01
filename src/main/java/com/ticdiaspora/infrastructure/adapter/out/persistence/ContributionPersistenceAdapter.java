package com.ticdiaspora.infrastructure.adapter.out.persistence;

import com.ticdiaspora.application.port.out.ContributionRepositoryPort;
import com.ticdiaspora.domain.model.*;
import com.ticdiaspora.domain.model.enums.*;
import com.ticdiaspora.infrastructure.adapter.out.persistence.mapper.ContributionPersistenceMapper;
import com.ticdiaspora.infrastructure.adapter.out.persistence.repository.ContributionJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.*;
import java.util.*;

@Repository
public class ContributionPersistenceAdapter implements ContributionRepositoryPort {

    private final ContributionJpaRepository repository;
    private final ContributionPersistenceMapper mapper;

    public ContributionPersistenceAdapter(ContributionJpaRepository repository, ContributionPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Page<Contribution> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDomain);
    }
    @Override
    public List<Contribution> findAll() {
        return mapper.toDomainList(repository.findAll());
    }
    @Override
    public Optional<Contribution> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }
    @Override
    public Contribution save(Contribution contribution) {
        return mapper.toDomain(repository.save(mapper.toEntity(contribution)));
    }
    @Override
    public List<Contribution> findAllByCycleId(UUID cycleId) {
        return mapper.toDomainList(repository.findAllByCycleId(cycleId));
    }
    @Override
    public List<Contribution> findAllByMemberId(UUID memberId) {
        return mapper.toDomainList(repository.findAllByMemberId(memberId));
    }
    @Override
    public Optional<Contribution> findByCycleIdAndMemberId(UUID cycleId, UUID memberId) {
        return repository.findByCycleIdAndMemberId(cycleId, memberId).map(mapper::toDomain);
    }
    @Override
    public long countByCycleIdAndStatusNot(UUID cycleId, ContributionStatus status) {
        return repository.countByCycleIdAndStatusNot(cycleId, status);
    }
}
