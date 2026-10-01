package com.ticdiaspora.project.infrastructure.persistence;

import com.ticdiaspora.shared.domain.enums.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProjectJpaRepository extends JpaRepository<ProjectEntity, UUID> {
    long countByStatus(ProjectStatus status);

    List<ProjectEntity> findTop5ByStatusOrderByCreatedAtDesc(ProjectStatus status);
}
