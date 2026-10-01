package com.ticdiaspora.beneficiary.infrastructure.web;

import com.ticdiaspora.auth.infrastructure.CurrentUserService;
import com.ticdiaspora.beneficiary.infrastructure.persistence.BeneficiaryConfirmationEntity;
import com.ticdiaspora.beneficiary.infrastructure.persistence.BeneficiaryConfirmationJpaRepository;
import com.ticdiaspora.shared.domain.enums.BeneficiaryConfirmationStatus;
import com.ticdiaspora.shared.domain.enums.NotificationType;
import com.ticdiaspora.shared.domain.enums.TontineCycleStatus;
import com.ticdiaspora.shared.domain.exception.NotFoundException;
import com.ticdiaspora.tontine.infrastructure.persistence.TontineCycleEntity;
import com.ticdiaspora.tontine.infrastructure.persistence.TontineCycleJpaRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/beneficiaries")
public class BeneficiaryController {

    private final BeneficiaryConfirmationJpaRepository confirmations;
    private final TontineCycleJpaRepository cycles;
    private final CurrentUserService currentUser;

    public BeneficiaryController(BeneficiaryConfirmationJpaRepository confirmations, TontineCycleJpaRepository cycles, CurrentUserService currentUser) {
        this.confirmations = confirmations;
        this.cycles = cycles;
        this.currentUser = currentUser;
    }

    @GetMapping("/{cycleId}")
    BeneficiaryResponse get(@PathVariable UUID cycleId) {
        return toResponse(confirmations.findByCycleId(cycleId).orElseThrow(() -> new NotFoundException("Confirmation bénéficiaire", cycleId)));
    }

    @PatchMapping("/{cycleId}/confirm")
    @PreAuthorize("hasRole('PRESIDENT') or authentication.token.claims['memberId'] == @beneficiaryController.beneficiaryId(#cycleId).toString()")
    BeneficiaryResponse confirm(@PathVariable UUID cycleId, @Valid @RequestBody BeneficiaryConfirmRequest request) {
        BeneficiaryConfirmationEntity confirmation = confirmations.findByCycleId(cycleId)
                .orElseThrow(() -> new NotFoundException("Confirmation bénéficiaire", cycleId));
        TontineCycleEntity cycle = cycles.findById(cycleId).orElseThrow(() -> new NotFoundException("Cycle de tontine", cycleId));
        confirmation.setReceivedAmount(request.receivedAmount());
        confirmation.setReceivedAt(Instant.now());
        confirmation.setBeneficiaryComment(request.comment());
        confirmation.setStatus(request.receivedAmount() >= confirmation.getExpectedAmount()
                ? BeneficiaryConfirmationStatus.FULLY_RECEIVED
                : BeneficiaryConfirmationStatus.PARTIAL_RECEIVED);
        confirmation.setFinalValidatedBy(currentUser.memberIdOrSystem());
        confirmation.setFinalValidatedAt(Instant.now());
        cycle.setTransferredAmount(request.receivedAmount());
        cycle.setRemainingAmount(Math.max(0, cycle.getExpectedAmount() - cycle.getCollectedAmount()));
        if (confirmation.getStatus() == BeneficiaryConfirmationStatus.FULLY_RECEIVED) {
            cycle.setStatus(TontineCycleStatus.PAID);
        }
        return toResponse(confirmation);
    }

    public UUID beneficiaryId(UUID cycleId) {
        return confirmations.findByCycleId(cycleId).map(BeneficiaryConfirmationEntity::getBeneficiaryId).orElse(null);
    }

    private BeneficiaryResponse toResponse(BeneficiaryConfirmationEntity confirmation) {
        return new BeneficiaryResponse(confirmation.getId(), confirmation.getCycleId(), confirmation.getBeneficiaryId(),
                confirmation.getExpectedAmount(), confirmation.getReceivedAmount(), confirmation.getReceivedAt(),
                confirmation.getStatus(), confirmation.getBeneficiaryComment(), confirmation.getFinalValidatedBy(),
                confirmation.getFinalValidatedAt());
    }

    public record BeneficiaryConfirmRequest(@PositiveOrZero long receivedAmount, String comment) {
    }

    public record BeneficiaryResponse(UUID id, UUID cycleId, UUID beneficiaryId, long expectedAmount, long receivedAmount,
                                      Instant receivedAt, BeneficiaryConfirmationStatus status, String beneficiaryComment,
                                      UUID finalValidatedBy, Instant finalValidatedAt) {
    }
}
