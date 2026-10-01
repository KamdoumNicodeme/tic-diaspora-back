package com.ticdiaspora.application.port.out;

import com.ticdiaspora.domain.model.*;
import com.ticdiaspora.domain.model.enums.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.*;
import java.util.*;

public interface ProjectRepositoryPort {
    Page<Project> findAll(Pageable pageable);

    Page<Project> findAllByStatus(ProjectStatus status, Pageable pageable);

    Optional<Project> findById(UUID id);

    Project save(Project project);

    long countByStatus(ProjectStatus status);

    List<Project> findTop5ByStatusOrderByCreatedAtDesc(ProjectStatus status);
}
