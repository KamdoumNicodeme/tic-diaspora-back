package com.ticdiaspora.infrastructure.adapter.in.web;

import com.ticdiaspora.application.port.out.*;

import com.ticdiaspora.infrastructure.security.CurrentUserService;
import com.ticdiaspora.domain.model.BeneficiaryConfirmation;
import com.ticdiaspora.application.port.out.BeneficiaryConfirmationRepositoryPort;
import com.ticdiaspora.domain.model.Contribution;
import com.ticdiaspora.domain.model.enums.BeneficiaryConfirmationStatus;
import com.ticdiaspora.domain.model.enums.TontineCycleStatus;
import com.ticdiaspora.domain.exception.NotFoundException;
import com.ticdiaspora.domain.model.TontineCycle;
import com.ticdiaspora.application.port.out.TontineCycleRepositoryPort;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/beneficiaries")
public class BeneficiaryController {

    private final BeneficiaryConfirmationRepositoryPort confirmations;
    private final TontineCycleRepositoryPort cycles;
    private final ContributionRepositoryPort contributions;
    private final CurrentUserService currentUser;

    public BeneficiaryController(BeneficiaryConfirmationRepositoryPort confirmations, TontineCycleRepositoryPort cycles,
                                 ContributionRepositoryPort contributions, CurrentUserService currentUser) {
        this.confirmations = confirmations;
        this.cycles = cycles;
        this.contributions = contributions;
        this.currentUser = currentUser;
    }

    @GetMapping("/{cycleId}")
    BeneficiaryResponse get(@PathVariable UUID cycleId) {
        return toResponse(confirmations.findByCycleId(cycleId).orElseThrow(() -> new NotFoundException("Confirmation bénéficiaire", cycleId)));
    }

    @PatchMapping("/{cycleId}/confirm")
    @PreAuthorize("hasRole('PRESIDENT') or @beneficiaryController.canConfirm(#cycleId, authentication.token.claims['memberId'])")
    BeneficiaryResponse confirm(@PathVariable UUID cycleId, @Valid @RequestBody BeneficiaryConfirmRequest request) {
        BeneficiaryConfirmation confirmation = confirmations.findByCycleId(cycleId)
                .orElseThrow(() -> new NotFoundException("Confirmation bénéficiaire", cycleId));
        TontineCycle cycle = cycles.findById(cycleId).orElseThrow(() -> new NotFoundException("Cycle de tontine", cycleId));
        long receivedAmount = contributions.findAllByCycleId(cycleId).stream().mapToLong(Contribution::getPaidAmount).sum();
        confirmation.setExpectedAmount(cycle.getExpectedAmount());
        confirmation.setReceivedAmount(receivedAmount);
        confirmation.setReceivedAt(Instant.now());
        confirmation.setBeneficiaryComment(request.comment());
        confirmation.setStatus(receivedAmount >= confirmation.getExpectedAmount()
                ? BeneficiaryConfirmationStatus.FULLY_RECEIVED
                : BeneficiaryConfirmationStatus.PARTIAL_RECEIVED);
        confirmation.setFinalValidatedBy(currentUser.memberIdOrSystem());
        confirmation.setFinalValidatedAt(Instant.now());
        confirmations.save(confirmation);
        cycle.setCollectedAmount(receivedAmount);
        cycle.setTransferredAmount(receivedAmount);
        cycle.setRemainingAmount(Math.max(0, cycle.getExpectedAmount() - receivedAmount));
        if (confirmation.getStatus() == BeneficiaryConfirmationStatus.FULLY_RECEIVED) {
            cycle.setStatus(TontineCycleStatus.PAID);
        }
        cycles.save(cycle);
        return toResponse(confirmation);
    }

    public UUID beneficiaryId(UUID cycleId) {
        return confirmations.findByCycleId(cycleId).map(BeneficiaryConfirmation::getBeneficiaryId).orElse(null);
    }

    public boolean canConfirm(UUID cycleId, Object memberIdClaim) {
        if (memberIdClaim == null) {
            return false;
        }
        UUID memberId = UUID.fromString(memberIdClaim.toString());
        BeneficiaryConfirmation confirmation = confirmations.findByCycleId(cycleId).orElse(null);
        TontineCycle cycle = cycles.findById(cycleId).orElse(null);
        return confirmation != null && Objects.equals(confirmation.getBeneficiaryId(), memberId)
                || cycle != null && Objects.equals(cycle.getSecondaryBeneficiaryId(), memberId);
    }

    private BeneficiaryResponse toResponse(BeneficiaryConfirmation confirmation) {
        return new BeneficiaryResponse(confirmation.getId(), confirmation.getCycleId(), confirmation.getBeneficiaryId(),
                confirmation.getExpectedAmount(), confirmation.getReceivedAmount(), confirmation.getReceivedAt(),
                confirmation.getStatus(), confirmation.getBeneficiaryComment(), confirmation.getFinalValidatedBy(),
                confirmation.getFinalValidatedAt());
    }

    public record BeneficiaryConfirmRequest(String comment) {
    }

    public record BeneficiaryResponse(UUID id, UUID cycleId, UUID beneficiaryId, long expectedAmount, long receivedAmount,
                                      Instant receivedAt, BeneficiaryConfirmationStatus status, String beneficiaryComment,
                                      UUID finalValidatedBy, Instant finalValidatedAt) {
    }
}
