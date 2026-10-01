package com.ticdiaspora.infrastructure.adapter.out.persistence.repository;

import com.ticdiaspora.infrastructure.adapter.out.persistence.entity.*;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TontineCycleJpaRepository extends JpaRepository<TontineCycleEntity, UUID> {
    Optional<TontineCycleEntity> findByMonthAndYear(int month, int year);
}
