package com.ticdiaspora.infrastructure.adapter.out.persistence;

import com.ticdiaspora.application.port.out.UserRepositoryPort;
import com.ticdiaspora.domain.model.*;
import com.ticdiaspora.domain.model.enums.*;
import com.ticdiaspora.infrastructure.adapter.out.persistence.mapper.UserPersistenceMapper;
import com.ticdiaspora.infrastructure.adapter.out.persistence.repository.UserJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.*;
import java.util.*;

@Repository
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final UserJpaRepository repository;
    private final UserPersistenceMapper mapper;

    public UserPersistenceAdapter(UserJpaRepository repository, UserPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<User> findByEmailIgnoreCase(String email) {
        return repository.findByEmailIgnoreCase(email).map(mapper::toDomain);
    }
    @Override
    public boolean existsByEmailIgnoreCase(String email) {
        return repository.existsByEmailIgnoreCase(email);
    }
    @Override
    public User save(User user) {
        return mapper.toDomain(repository.save(mapper.toEntity(user)));
    }
}
