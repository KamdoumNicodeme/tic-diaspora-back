package com.ticdiaspora.infrastructure.adapter.out.persistence;

import com.ticdiaspora.application.port.out.BeneficiaryConfirmationRepositoryPort;
import com.ticdiaspora.domain.model.*;
import com.ticdiaspora.domain.model.enums.*;
import com.ticdiaspora.infrastructure.adapter.out.persistence.mapper.BeneficiaryConfirmationPersistenceMapper;
import com.ticdiaspora.infrastructure.adapter.out.persistence.repository.BeneficiaryConfirmationJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.*;
import java.util.*;

@Repository
public class BeneficiaryConfirmationPersistenceAdapter implements BeneficiaryConfirmationRepositoryPort {

    private final BeneficiaryConfirmationJpaRepository repository;
    private final BeneficiaryConfirmationPersistenceMapper mapper;

    public BeneficiaryConfirmationPersistenceAdapter(BeneficiaryConfirmationJpaRepository repository, BeneficiaryConfirmationPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<BeneficiaryConfirmation> findByCycleId(UUID cycleId) {
        return repository.findByCycleId(cycleId).map(mapper::toDomain);
    }
    @Override
    public BeneficiaryConfirmation save(BeneficiaryConfirmation beneficiaryConfirmation) {
        return mapper.toDomain(repository.save(mapper.toEntity(beneficiaryConfirmation)));
    }
}
