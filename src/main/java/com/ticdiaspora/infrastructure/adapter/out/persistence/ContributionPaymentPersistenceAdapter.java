package com.ticdiaspora.infrastructure.adapter.out.persistence;

import com.ticdiaspora.application.port.out.ContributionPaymentRepositoryPort;
import com.ticdiaspora.domain.model.*;
import com.ticdiaspora.domain.model.enums.*;
import com.ticdiaspora.infrastructure.adapter.out.persistence.mapper.ContributionPaymentPersistenceMapper;
import com.ticdiaspora.infrastructure.adapter.out.persistence.repository.ContributionPaymentJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.*;
import java.util.*;

@Repository
public class ContributionPaymentPersistenceAdapter implements ContributionPaymentRepositoryPort {

    private final ContributionPaymentJpaRepository repository;
    private final ContributionPaymentPersistenceMapper mapper;

    public ContributionPaymentPersistenceAdapter(ContributionPaymentJpaRepository repository, ContributionPaymentPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public ContributionPayment save(ContributionPayment contributionPayment) {
        return mapper.toDomain(repository.save(mapper.toEntity(contributionPayment)));
    }
    @Override
    public List<ContributionPayment> findAllByContributionId(UUID contributionId) {
        return mapper.toDomainList(repository.findAllByContributionId(contributionId));
    }
    @Override
    public Optional<ContributionPayment> findFirstByContributionIdOrderByPaidAtDesc(UUID contributionId) {
        return repository.findFirstByContributionIdOrderByPaidAtDesc(contributionId).map(mapper::toDomain);
    }

    @Override
    public Optional<ContributionPayment> findByProofUrl(String proofUrl) {
        return repository.findByProofUrl(proofUrl).map(mapper::toDomain);
    }
}
