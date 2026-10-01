package com.ticdiaspora.infrastructure.adapter.out.persistence.repository;

import com.ticdiaspora.infrastructure.adapter.out.persistence.entity.*;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
