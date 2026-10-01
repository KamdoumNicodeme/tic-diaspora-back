package com.ticdiaspora.project.infrastructure.web;

import com.ticdiaspora.project.infrastructure.persistence.ProjectEntity;
import com.ticdiaspora.project.infrastructure.persistence.ProjectJpaRepository;
import com.ticdiaspora.shared.domain.enums.ProjectStatus;
import com.ticdiaspora.shared.domain.exception.NotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectJpaRepository projects;

    public ProjectController(ProjectJpaRepository projects) {
        this.projects = projects;
    }

    @GetMapping
    Page<ProjectResponse> list(Pageable pageable) {
        return projects.findAll(pageable).map(this::toResponse);
    }

    @PostMapping
    @PreAuthorize("hasRole('PRESIDENT')")
    ProjectResponse create(@Valid @RequestBody ProjectRequest request) {
        ProjectEntity project = new ProjectEntity();
        apply(request, project);
        return toResponse(projects.save(project));
    }

    @GetMapping("/{id}")
    ProjectResponse get(@PathVariable UUID id) {
        return toResponse(find(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PRESIDENT')")
    ProjectResponse update(@PathVariable UUID id, @Valid @RequestBody ProjectRequest request) {
        ProjectEntity project = find(id);
        apply(request, project);
        return toResponse(projects.save(project));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('PRESIDENT')")
    ProjectResponse changeStatus(@PathVariable UUID id, @RequestBody ChangeProjectStatusRequest request) {
        ProjectEntity project = find(id);
        project.setStatus(request.status());
        return toResponse(projects.save(project));
    }

    private ProjectEntity find(UUID id) {
        return projects.findById(id).orElseThrow(() -> new NotFoundException("Projet", id));
    }

    private void apply(ProjectRequest request, ProjectEntity project) {
        project.setTitle(request.title());
        project.setDescription(request.description());
        project.setResponsibleId(request.responsibleId());
        project.setStatus(request.status() == null ? ProjectStatus.PROPOSED : request.status());
        project.setEstimatedBudget(request.estimatedBudget());
        project.setRealBudget(request.realBudget());
        project.setStartDate(request.startDate());
        project.setEndDate(request.endDate());
    }

    private ProjectResponse toResponse(ProjectEntity project) {
        return new ProjectResponse(project.getId(), project.getTitle(), project.getDescription(), project.getResponsibleId(),
                project.getStatus(), project.getEstimatedBudget(), project.getRealBudget(), project.getCreatedAt(),
                project.getStartDate(), project.getEndDate());
    }

    public record ProjectRequest(@NotBlank String title, @NotBlank String description, UUID responsibleId,
                                 ProjectStatus status, long estimatedBudget, long realBudget,
                                 LocalDate startDate, LocalDate endDate) {
    }

    public record ChangeProjectStatusRequest(ProjectStatus status) {
    }

    public record ProjectResponse(UUID id, String title, String description, UUID responsibleId, ProjectStatus status,
                                  long estimatedBudget, long realBudget, Instant createdAt, LocalDate startDate, LocalDate endDate) {
    }
}
