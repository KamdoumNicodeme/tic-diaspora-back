package com.ticdiaspora.infrastructure.adapter.out.persistence;

import com.ticdiaspora.application.port.out.AuditLogRepositoryPort;
import com.ticdiaspora.domain.model.*;
import com.ticdiaspora.domain.model.enums.*;
import com.ticdiaspora.infrastructure.adapter.out.persistence.mapper.AuditLogPersistenceMapper;
import com.ticdiaspora.infrastructure.adapter.out.persistence.repository.AuditLogJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.*;
import java.util.*;

@Repository
public class AuditLogPersistenceAdapter implements AuditLogRepositoryPort {

    private final AuditLogJpaRepository repository;
    private final AuditLogPersistenceMapper mapper;

    public AuditLogPersistenceAdapter(AuditLogJpaRepository repository, AuditLogPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Page<AuditLog> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDomain);
    }
    @Override
    public AuditLog save(AuditLog auditLog) {
        return mapper.toDomain(repository.save(mapper.toEntity(auditLog)));
    }
}
