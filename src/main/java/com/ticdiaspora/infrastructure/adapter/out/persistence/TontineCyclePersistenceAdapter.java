package com.ticdiaspora.infrastructure.adapter.out.persistence;

import com.ticdiaspora.application.port.out.TontineCycleRepositoryPort;
import com.ticdiaspora.domain.model.*;
import com.ticdiaspora.domain.model.enums.*;
import com.ticdiaspora.infrastructure.adapter.out.persistence.mapper.TontineCyclePersistenceMapper;
import com.ticdiaspora.infrastructure.adapter.out.persistence.repository.TontineCycleJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.*;
import java.util.*;

@Repository
public class TontineCyclePersistenceAdapter implements TontineCycleRepositoryPort {

    private final TontineCycleJpaRepository repository;
    private final TontineCyclePersistenceMapper mapper;

    public TontineCyclePersistenceAdapter(TontineCycleJpaRepository repository, TontineCyclePersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Page<TontineCycle> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDomain);
    }

    @Override
    public List<TontineCycle> findAll() {
        return mapper.toDomainList(repository.findAll());
    }
    @Override
    public Optional<TontineCycle> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }
    @Override
    public TontineCycle save(TontineCycle tontineCycle) {
        return mapper.toDomain(repository.save(mapper.toEntity(tontineCycle)));
    }
    @Override
    public Optional<TontineCycle> findByMonthAndYear(int month, int year) {
        return repository.findByMonthAndYear(month, year).map(mapper::toDomain);
    }
}
