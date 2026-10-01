package com.ticdiaspora.infrastructure.adapter.out.persistence;

import com.ticdiaspora.application.port.out.MemberRepositoryPort;
import com.ticdiaspora.domain.model.*;
import com.ticdiaspora.domain.model.enums.*;
import com.ticdiaspora.infrastructure.adapter.out.persistence.mapper.MemberPersistenceMapper;
import com.ticdiaspora.infrastructure.adapter.out.persistence.repository.MemberJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.*;
import java.util.*;

@Repository
public class MemberPersistenceAdapter implements MemberRepositoryPort {

    private final MemberJpaRepository repository;
    private final MemberPersistenceMapper mapper;

    public MemberPersistenceAdapter(MemberJpaRepository repository, MemberPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Page<Member> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDomain);
    }
    @Override
    public List<Member> findAll() {
        return mapper.toDomainList(repository.findAll());
    }
    @Override
    public Optional<Member> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }
    @Override
    public Member save(Member member) {
        return mapper.toDomain(repository.save(mapper.toEntity(member)));
    }
    @Override
    public Optional<Member> findByEmailIgnoreCase(String email) {
        return repository.findByEmailIgnoreCase(email).map(mapper::toDomain);
    }
    @Override
    public boolean existsByEmailIgnoreCase(String email) {
        return repository.existsByEmailIgnoreCase(email);
    }
    @Override
    public List<Member> findAllByStatus(MemberStatus status) {
        return mapper.toDomainList(repository.findAllByStatus(status));
    }
    @Override
    public long countByStatus(MemberStatus status) {
        return repository.countByStatus(status);
    }
    @Override
    public long count() {
        return repository.count();
    }
}
