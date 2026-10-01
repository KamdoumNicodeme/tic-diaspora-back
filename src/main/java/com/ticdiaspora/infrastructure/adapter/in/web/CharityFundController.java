package com.ticdiaspora.infrastructure.adapter.in.web;

import com.ticdiaspora.application.usecase.*;
import com.ticdiaspora.application.port.out.*;
import com.ticdiaspora.infrastructure.adapter.in.web.dto.*;

import com.ticdiaspora.application.usecase.AuditService;
import com.ticdiaspora.infrastructure.security.CurrentUserService;
import com.ticdiaspora.domain.model.CharityFundMovement;
import com.ticdiaspora.domain.model.Member;
import com.ticdiaspora.domain.model.Project;
import com.ticdiaspora.application.port.out.CharityFundMovementRepositoryPort;
import com.ticdiaspora.application.port.out.MemberRepositoryPort;
import com.ticdiaspora.application.port.out.ProjectRepositoryPort;
import com.ticdiaspora.domain.model.enums.AuditAction;
import com.ticdiaspora.domain.model.enums.CharityMovementType;
import com.ticdiaspora.domain.exception.BusinessException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

@RestController
@RequestMapping("/api/charity-fund")
public class CharityFundController {

    private final CharityFundMovementRepositoryPort movements;
    private final MemberRepositoryPort members;
    private final ProjectRepositoryPort projects;
    private final CurrentUserService currentUser;
    private final AuditService auditService;

    public CharityFundController(
            CharityFundMovementRepositoryPort movements,
            MemberRepositoryPort members,
            ProjectRepositoryPort projects,
            CurrentUserService currentUser,
            AuditService auditService
    ) {
        this.movements = movements;
        this.members = members;
        this.projects = projects;
        this.currentUser = currentUser;
        this.auditService = auditService;
    }

    @GetMapping
    @PreAuthorize("hasRole('PRESIDENT')")
    CharitySummary summary() {
        int year = LocalDate.now().getYear();
        Instant start = LocalDate.of(year, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant end = LocalDate.of(year + 1, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        return new CharitySummary(
                movements.balance(),
                movements.totalIncome(),
                movements.totalOutcome(),
                movements.totalIncomeBetween(start, end),
                movements.totalOutcomeBetween(start, end),
                movements.count()
        );
    }

    @GetMapping("/movements")
    @PreAuthorize("hasRole('PRESIDENT')")
    Page<CharityMovementResponse> movements(Pageable pageable) {
        return movements.findAll(pageable).map(this::toResponse);
    }

    @PostMapping("/outcomes")
    @PreAuthorize("hasRole('PRESIDENT')")
    CharityMovementResponse outcome(@Valid @RequestBody CharityOutcomeRequest request) {
        if (request.amount() > movements.balance()) {
            throw new BusinessException("CHARITY_BALANCE_TOO_LOW", "La sortie demandée dépasse le solde disponible de la caisse caritative");
        }
        CharityFundMovement movement = new CharityFundMovement();
        movement.setProjectId(request.projectId());
        movement.setType(CharityMovementType.CHARITY_OUTCOME);
        movement.setAmount(-request.amount());
        movement.setDescription(request.description());
        movement.setMovementDate(Instant.now());
        movement.setCreatedBy(currentUser.memberIdOrSystem());
        movement = movements.save(movement);
        auditService.record(AuditAction.CHARITY_OUTCOME_CREATED, "CharityFundMovement", movement.getId(), null,
                String.valueOf(movement.getAmount()), request.description());
        return toResponse(movement);
    }

    private CharityMovementResponse toResponse(CharityFundMovement movement) {
        Member member = movement.getMemberId() == null ? null : members.findById(movement.getMemberId()).orElse(null);
        Project project = movement.getProjectId() == null ? null : projects.findById(movement.getProjectId()).orElse(null);
        Member createdBy = movement.getCreatedBy() == null ? null : members.findById(movement.getCreatedBy()).orElse(null);
        return new CharityMovementResponse(movement.getId(), movement.getMemberId(), movement.getPenaltyId(),
                movement.getProjectId(),
                member == null ? null : member.getFirstName() + " " + member.getLastName(),
                project == null ? null : project.getTitle(),
                movement.getType(), movement.getAmount(), movement.getDescription(),
                movement.getMovementDate(), movement.getCreatedBy(),
                createdBy == null ? null : createdBy.getFirstName() + " " + createdBy.getLastName());
    }

    public record CharitySummary(
            long balance,
            long totalIncome,
            long totalOutcome,
            long currentYearIncome,
            long currentYearOutcome,
            long movementsCount
    ) {
    }

    public record CharityOutcomeRequest(UUID projectId, @Positive long amount, @NotBlank String description) {
    }

    public record CharityMovementResponse(UUID id, UUID memberId, UUID penaltyId, UUID projectId,
                                          String memberFullName, String projectTitle, CharityMovementType type,
                                          long amount, String description, Instant movementDate, UUID createdBy,
                                          String createdByFullName) {
    }
}
