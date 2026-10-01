package com.ticdiaspora.infrastructure.adapter.in.web;

import com.ticdiaspora.application.usecase.*;
import com.ticdiaspora.application.port.out.*;
import com.ticdiaspora.infrastructure.adapter.in.web.dto.*;

import com.ticdiaspora.domain.model.Project;
import com.ticdiaspora.domain.model.Member;
import com.ticdiaspora.application.port.out.ProjectRepositoryPort;
import com.ticdiaspora.domain.model.enums.ProjectStatus;
import com.ticdiaspora.domain.model.enums.DecisionStatus;
import com.ticdiaspora.domain.exception.NotFoundException;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectRepositoryPort projects;
    private final MemberRepositoryPort members;
    private final DecisionRepositoryPort decisions;

    public ProjectController(ProjectRepositoryPort projects, MemberRepositoryPort members, DecisionRepositoryPort decisions) {
        this.projects = projects;
        this.members = members;
        this.decisions = decisions;
    }

    @GetMapping
    Page<ProjectResponse> list(@RequestParam(required = false) ProjectStatus status, Pageable pageable) {
        Page<Project> page = status == null ? projects.findAll(pageable) : projects.findAllByStatus(status, pageable);
        return page.map(this::toResponse);
    }

    @PostMapping
    @PreAuthorize("hasRole('PRESIDENT')")
    ProjectResponse create(@Valid @RequestBody ProjectRequest request) {
        Project project = new Project();
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
        Project project = find(id);
        apply(request, project);
        return toResponse(projects.save(project));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('PRESIDENT')")
    ProjectResponse changeStatus(@PathVariable UUID id, @RequestBody ChangeProjectStatusRequest request) {
        Project project = find(id);
        project.setStatus(request.status());
        return toResponse(projects.save(project));
    }

    private Project find(UUID id) {
        return projects.findById(id).orElseThrow(() -> new NotFoundException("Projet", id));
    }

    private void apply(ProjectRequest request, Project project) {
        project.setTitle(request.title());
        project.setDescription(request.description());
        project.setResponsibleId(request.responsibleId());
        project.setStatus(request.status() == null ? ProjectStatus.PROPOSED : request.status());
        project.setEstimatedBudget(request.estimatedBudget());
        project.setRealBudget(request.realBudget());
        project.setStartDate(request.startDate());
        project.setEndDate(request.endDate());
    }

    private ProjectResponse toResponse(Project project) {
        Member responsible = project.getResponsibleId() == null ? null : members.findById(project.getResponsibleId()).orElse(null);
        long decisionsCount = decisions.countByProjectId(project.getId());
        long openDecisionsCount = decisions.countByProjectIdAndStatusIn(
                project.getId(),
                List.of(DecisionStatus.OPEN, DecisionStatus.IN_PROGRESS)
        );
        return new ProjectResponse(project.getId(), project.getTitle(), project.getDescription(), project.getResponsibleId(),
                responsible == null ? null : responsible.getFirstName() + " " + responsible.getLastName(),
                project.getStatus(), project.getEstimatedBudget(), project.getRealBudget(), project.getCreatedAt(),
                project.getStartDate(), project.getEndDate(), decisionsCount, openDecisionsCount);
    }

    public record ProjectRequest(@NotBlank String title, @NotBlank String description, UUID responsibleId,
                                 ProjectStatus status, long estimatedBudget, long realBudget,
                                 LocalDate startDate, LocalDate endDate) {
    }

    public record ChangeProjectStatusRequest(ProjectStatus status) {
    }

    public record ProjectResponse(UUID id, String title, String description, UUID responsibleId, String responsibleFullName,
                                  ProjectStatus status, long estimatedBudget, long realBudget, Instant createdAt,
                                  LocalDate startDate, LocalDate endDate, long decisionsCount, long openDecisionsCount) {
    }
}
