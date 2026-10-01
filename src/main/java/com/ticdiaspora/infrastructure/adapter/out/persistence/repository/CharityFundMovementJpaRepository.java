package com.ticdiaspora.infrastructure.adapter.out.persistence.repository;

import com.ticdiaspora.infrastructure.adapter.out.persistence.entity.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface CharityFundMovementJpaRepository extends JpaRepository<CharityFundMovementEntity, UUID> {
    @Query("select coalesce(sum(m.amount), 0) from CharityFundMovementEntity m")
    long balance();

    @Query("select coalesce(sum(m.amount), 0) from CharityFundMovementEntity m where m.amount > 0")
    long totalIncome();

    @Query("select coalesce(sum(-m.amount), 0) from CharityFundMovementEntity m where m.amount < 0")
    long totalOutcome();

    @Query("""
            select coalesce(sum(m.amount), 0)
            from CharityFundMovementEntity m
            where m.amount > 0 and m.movementDate >= :start and m.movementDate < :end
            """)
    long totalIncomeBetween(@Param("start") Instant start, @Param("end") Instant end);

    @Query("""
            select coalesce(sum(-m.amount), 0)
            from CharityFundMovementEntity m
            where m.amount < 0 and m.movementDate >= :start and m.movementDate < :end
            """)
    long totalOutcomeBetween(@Param("start") Instant start, @Param("end") Instant end);
}
