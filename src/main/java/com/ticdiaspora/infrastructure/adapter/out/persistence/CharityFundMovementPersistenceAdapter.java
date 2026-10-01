package com.ticdiaspora.infrastructure.adapter.out.persistence;

import com.ticdiaspora.application.port.out.CharityFundMovementRepositoryPort;
import com.ticdiaspora.domain.model.*;
import com.ticdiaspora.domain.model.enums.*;
import com.ticdiaspora.infrastructure.adapter.out.persistence.mapper.CharityFundMovementPersistenceMapper;
import com.ticdiaspora.infrastructure.adapter.out.persistence.repository.CharityFundMovementJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.*;
import java.util.*;

@Repository
public class CharityFundMovementPersistenceAdapter implements CharityFundMovementRepositoryPort {

    private final CharityFundMovementJpaRepository repository;
    private final CharityFundMovementPersistenceMapper mapper;

    public CharityFundMovementPersistenceAdapter(CharityFundMovementJpaRepository repository, CharityFundMovementPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Page<CharityFundMovement> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDomain);
    }
    @Override
    public CharityFundMovement save(CharityFundMovement charityFundMovement) {
        return mapper.toDomain(repository.save(mapper.toEntity(charityFundMovement)));
    }
    @Override
    public long balance() {
        return repository.balance();
    }
    @Override
    public long totalIncome() {
        return repository.totalIncome();
    }
    @Override
    public long totalOutcome() {
        return repository.totalOutcome();
    }
    @Override
    public long totalIncomeBetween(Instant start, Instant end) {
        return repository.totalIncomeBetween(start, end);
    }
    @Override
    public long totalOutcomeBetween(Instant start, Instant end) {
        return repository.totalOutcomeBetween(start, end);
    }
    @Override
    public long count() {
        return repository.count();
    }
}
