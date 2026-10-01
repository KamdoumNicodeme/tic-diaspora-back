package com.ticdiaspora.infrastructure.adapter.out.persistence;

import com.ticdiaspora.application.port.out.ProjectRepositoryPort;
import com.ticdiaspora.domain.model.*;
import com.ticdiaspora.domain.model.enums.*;
import com.ticdiaspora.infrastructure.adapter.out.persistence.mapper.ProjectPersistenceMapper;
import com.ticdiaspora.infrastructure.adapter.out.persistence.repository.ProjectJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.*;
import java.util.*;

@Repository
public class ProjectPersistenceAdapter implements ProjectRepositoryPort {

    private final ProjectJpaRepository repository;
    private final ProjectPersistenceMapper mapper;

    public ProjectPersistenceAdapter(ProjectJpaRepository repository, ProjectPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Page<Project> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDomain);
    }
    @Override
    public Page<Project> findAllByStatus(ProjectStatus status, Pageable pageable) {
        return repository.findAllByStatus(status, pageable).map(mapper::toDomain);
    }
    @Override
    public Optional<Project> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }
    @Override
    public Project save(Project project) {
        return mapper.toDomain(repository.save(mapper.toEntity(project)));
    }
    @Override
    public long countByStatus(ProjectStatus status) {
        return repository.countByStatus(status);
    }
    @Override
    public List<Project> findTop5ByStatusOrderByCreatedAtDesc(ProjectStatus status) {
        return mapper.toDomainList(repository.findTop5ByStatusOrderByCreatedAtDesc(status));
    }
}
