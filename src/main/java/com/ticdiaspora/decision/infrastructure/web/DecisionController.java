package com.ticdiaspora.decision.infrastructure.web;

import com.ticdiaspora.decision.infrastructure.persistence.DecisionEntity;
import com.ticdiaspora.decision.infrastructure.persistence.DecisionJpaRepository;
import com.ticdiaspora.shared.domain.enums.DecisionPriority;
import com.ticdiaspora.shared.domain.enums.DecisionStatus;
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
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/decisions")
public class DecisionController {

    private final DecisionJpaRepository decisions;

    public DecisionController(DecisionJpaRepository decisions) {
        this.decisions = decisions;
    }

    @GetMapping
    Page<DecisionResponse> list(Pageable pageable) {
        return decisions.findAll(pageable).map(this::toResponse);
    }

    @GetMapping("/open")
    List<DecisionResponse> open() {
        return decisions.findTop5ByStatusInOrderByDueDateAsc(List.of(DecisionStatus.OPEN, DecisionStatus.IN_PROGRESS))
                .stream().map(this::toResponse).toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('PRESIDENT')")
    DecisionResponse create(@Valid @RequestBody DecisionRequest request) {
        DecisionEntity decision = new DecisionEntity();
        apply(request, decision);
        return toResponse(decisions.save(decision));
    }

    @GetMapping("/{id}")
    DecisionResponse get(@PathVariable UUID id) {
        return toResponse(find(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PRESIDENT')")
    DecisionResponse update(@PathVariable UUID id, @Valid @RequestBody DecisionRequest request) {
        DecisionEntity decision = find(id);
        apply(request, decision);
        return toResponse(decisions.save(decision));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('PRESIDENT')")
    DecisionResponse changeStatus(@PathVariable UUID id, @RequestBody ChangeDecisionStatusRequest request) {
        DecisionEntity decision = find(id);
        decision.setStatus(request.status());
        return toResponse(decisions.save(decision));
    }

    private DecisionEntity find(UUID id) {
        return decisions.findById(id).orElseThrow(() -> new NotFoundException("Décision", id));
    }

    private void apply(DecisionRequest request, DecisionEntity decision) {
        decision.setMeetingId(request.meetingId());
        decision.setProjectId(request.projectId());
        decision.setTitle(request.title());
        decision.setDescription(request.description());
        decision.setResponsibleId(request.responsibleId());
        decision.setDueDate(request.dueDate());
        decision.setStatus(request.status() == null ? DecisionStatus.OPEN : request.status());
        decision.setPriority(request.priority() == null ? DecisionPriority.MEDIUM : request.priority());
    }

    private DecisionResponse toResponse(DecisionEntity decision) {
        return new DecisionResponse(decision.getId(), decision.getMeetingId(), decision.getProjectId(), decision.getTitle(),
                decision.getDescription(), decision.getResponsibleId(), decision.getDueDate(), decision.getStatus(),
                decision.getPriority(), decision.getCreatedAt(), decision.getUpdatedAt());
    }

    public record DecisionRequest(UUID meetingId, UUID projectId, @NotBlank String title, @NotBlank String description,
                                  UUID responsibleId, LocalDate dueDate, DecisionStatus status, DecisionPriority priority) {
    }

    public record ChangeDecisionStatusRequest(DecisionStatus status) {
    }

    public record DecisionResponse(UUID id, UUID meetingId, UUID projectId, String title, String description,
                                   UUID responsibleId, LocalDate dueDate, DecisionStatus status, DecisionPriority priority,
                                   Instant createdAt, Instant updatedAt) {
    }
}
