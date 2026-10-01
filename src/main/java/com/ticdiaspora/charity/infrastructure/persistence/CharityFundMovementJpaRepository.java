package com.ticdiaspora.charity.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface CharityFundMovementJpaRepository extends JpaRepository<CharityFundMovementEntity, UUID> {
    @Query("select coalesce(sum(m.amount), 0) from CharityFundMovementEntity m")
    long balance();
}
