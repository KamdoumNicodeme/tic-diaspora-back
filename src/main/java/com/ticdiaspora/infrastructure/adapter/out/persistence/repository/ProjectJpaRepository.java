package com.ticdiaspora.infrastructure.adapter.out.persistence.repository;

import com.ticdiaspora.infrastructure.adapter.out.persistence.entity.*;

import com.ticdiaspora.domain.model.enums.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProjectJpaRepository extends JpaRepository<ProjectEntity, UUID> {
    Page<ProjectEntity> findAllByStatus(ProjectStatus status, Pageable pageable);

    long countByStatus(ProjectStatus status);

    List<ProjectEntity> findTop5ByStatusOrderByCreatedAtDesc(ProjectStatus status);
}
