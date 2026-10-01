package com.ticdiaspora.member.infrastructure.persistence;

import com.ticdiaspora.shared.domain.enums.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MemberJpaRepository extends JpaRepository<MemberEntity, UUID> {
    Optional<MemberEntity> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<MemberEntity> findAllByStatus(MemberStatus status);

    long countByStatus(MemberStatus status);
}
