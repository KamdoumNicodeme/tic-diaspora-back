package com.ticdiaspora.charity.infrastructure.web;

import com.ticdiaspora.audit.infrastructure.web.AuditService;
import com.ticdiaspora.auth.infrastructure.CurrentUserService;
import com.ticdiaspora.charity.infrastructure.persistence.CharityFundMovementEntity;
import com.ticdiaspora.charity.infrastructure.persistence.CharityFundMovementJpaRepository;
import com.ticdiaspora.shared.domain.enums.AuditAction;
import com.ticdiaspora.shared.domain.enums.CharityMovementType;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/charity-fund")
public class CharityFundController {

    private final CharityFundMovementJpaRepository movements;
    private final CurrentUserService currentUser;
    private final AuditService auditService;

    public CharityFundController(CharityFundMovementJpaRepository movements, CurrentUserService currentUser, AuditService auditService) {
        this.movements = movements;
        this.currentUser = currentUser;
        this.auditService = auditService;
    }

    @GetMapping
    @PreAuthorize("hasRole('PRESIDENT')")
    CharitySummary summary() {
        return new CharitySummary(movements.balance());
    }

    @GetMapping("/movements")
    @PreAuthorize("hasRole('PRESIDENT')")
    Page<CharityMovementResponse> movements(Pageable pageable) {
        return movements.findAll(pageable).map(this::toResponse);
    }

    @PostMapping("/outcomes")
    @PreAuthorize("hasRole('PRESIDENT')")
    CharityMovementResponse outcome(@Valid @RequestBody CharityOutcomeRequest request) {
        CharityFundMovementEntity movement = new CharityFundMovementEntity();
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

    private CharityMovementResponse toResponse(CharityFundMovementEntity movement) {
        return new CharityMovementResponse(movement.getId(), movement.getMemberId(), movement.getPenaltyId(),
                movement.getProjectId(), movement.getType(), movement.getAmount(), movement.getDescription(),
                movement.getMovementDate(), movement.getCreatedBy());
    }

    public record CharitySummary(long balance) {
    }

    public record CharityOutcomeRequest(UUID projectId, @Positive long amount, @NotBlank String description) {
    }

    public record CharityMovementResponse(UUID id, UUID memberId, UUID penaltyId, UUID projectId, CharityMovementType type,
                                          long amount, String description, Instant movementDate, UUID createdBy) {
    }
}
