package com.ticdiaspora.application.usecase;
import com.ticdiaspora.application.port.in.*;
import com.ticdiaspora.application.port.out.*;

import com.ticdiaspora.application.port.out.CurrentUserPort;
import com.ticdiaspora.domain.model.Contribution;
import com.ticdiaspora.application.port.out.ContributionRepositoryPort;
import com.ticdiaspora.domain.model.ContributionPayment;
import com.ticdiaspora.application.port.out.ContributionPaymentRepositoryPort;
import com.ticdiaspora.domain.model.enums.AuditAction;
import com.ticdiaspora.domain.model.enums.NotificationType;
import com.ticdiaspora.domain.model.enums.PaymentMethod;
import com.ticdiaspora.domain.model.enums.TontineCycleStatus;
import com.ticdiaspora.domain.exception.BusinessException;
import com.ticdiaspora.domain.exception.NotFoundException;
import com.ticdiaspora.domain.service.TontineRules;
import com.ticdiaspora.domain.model.TontineCycle;
import com.ticdiaspora.application.port.out.TontineCycleRepositoryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.ticdiaspora.application.annotation.UseCase;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@UseCase
public class ContributionService {

    private final ContributionRepositoryPort contributions;
    private final ContributionPaymentRepositoryPort payments;
    private final TontineCycleRepositoryPort cycles;
    private final TontineRules tontineRules;
    private final CurrentUserPort currentUser;
    private final AuditService auditService;
    private final NotificationService notificationService;

    public ContributionService(
            ContributionRepositoryPort contributions,
            ContributionPaymentRepositoryPort payments,
            TontineCycleRepositoryPort cycles,
            TontineRules tontineRules,
            CurrentUserPort currentUser,
            AuditService auditService,
            NotificationService notificationService
    ) {
        this.contributions = contributions;
        this.payments = payments;
        this.cycles = cycles;
        this.tontineRules = tontineRules;
        this.currentUser = currentUser;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public Page<Contribution> list(Pageable pageable) {
        return contributions.findAll(pageable).map(this::withLatestPayment);
    }

    @Transactional(readOnly = true)
    public Contribution get(UUID id) {
        return withLatestPayment(contributions.findById(id).orElseThrow(() -> new NotFoundException("Cotisation", id)));
    }

    @Transactional(readOnly = true)
    public java.util.List<Contribution> byCycle(UUID cycleId) {
        return contributions.findAllByCycleId(cycleId).stream().map(this::withLatestPayment).toList();
    }

    @Transactional(readOnly = true)
    public java.util.List<Contribution> byMember(UUID memberId) {
        return contributions.findAllByMemberId(memberId).stream().map(this::withLatestPayment).toList();
    }

    @Transactional
    public Contribution recordPayment(UUID cycleId, UUID memberId, long amount, PaymentMethod method, String reference, String proofUrl) {
        Contribution contribution = contributions.findByCycleIdAndMemberId(cycleId, memberId)
                .orElseThrow(() -> new NotFoundException("Cotisation du membre dans le cycle", memberId));
        if (contribution.getStatus().name().equals("CANCELLED")) {
            throw new BusinessException("CONTRIBUTION_CANCELLED", "Une cotisation annulée ne peut pas être payée");
        }
        ContributionPayment payment = new ContributionPayment();
        payment.setContributionId(contribution.getId());
        payment.setAmount(amount);
        payment.setMethod(method);
        payment.setTransactionReference(reference);
        payment.setProofUrl(proofUrl);
        payment.setPaidAt(Instant.now());
        payments.save(payment);

        contribution.setPaidAmount(contribution.getPaidAmount() + amount);
        contribution.setPaidAt(Instant.now());
        contribution.setStatus(tontineRules.contributionStatus(contribution.getExpectedAmount(), contribution.getPaidAmount()));
        contribution = contributions.save(contribution);
        refreshCycleAmounts(cycleId);
        return withLatestPayment(contribution);
    }

    @Transactional
    public Contribution validate(UUID id) {
        Contribution contribution = get(id);
        contribution.setValidatedBy(currentUser.memberIdOrSystem());
        contribution.setValidatedAt(Instant.now());
        contribution = contributions.save(contribution);
        auditService.record(AuditAction.CONTRIBUTION_VALIDATED, "Contribution", contribution.getId(), null,
                contribution.getStatus().name(), "Validation cotisation");
        notificationService.notifyInApp(contribution.getMemberId(), NotificationType.CONTRIBUTION_VALIDATED,
                "Cotisation validée", "Votre cotisation a été validée.", "{\"contributionId\":\"" + contribution.getId() + "\"}");
        refreshCycleAmounts(contribution.getCycleId());
        return withLatestPayment(contribution);
    }

    @Transactional(readOnly = true)
    public boolean canCurrentUserAccessProof(String fileName) {
        UUID currentMemberId = currentUser.memberIdOrSystem();
        if (currentMemberId == null) {
            return false;
        }
        String proofUrl = "/contributions/proofs/" + fileName;
        return payments.findByProofUrl(proofUrl)
                .flatMap(payment -> contributions.findById(payment.getContributionId()))
                .map(contribution -> currentMemberId.equals(contribution.getMemberId()))
                .orElse(false);
    }

    private Contribution withLatestPayment(Contribution contribution) {
        payments.findFirstByContributionIdOrderByPaidAtDesc(contribution.getId()).ifPresent(payment -> {
            contribution.setLastPaymentMethod(payment.getMethod());
            contribution.setLastTransactionReference(payment.getTransactionReference());
            contribution.setLastProofUrl(payment.getProofUrl());
            contribution.setLastPaymentAt(payment.getPaidAt());
        });
        return contribution;
    }

    private void refreshCycleAmounts(UUID cycleId) {
        TontineCycle cycle = cycles.findById(cycleId).orElseThrow(() -> new NotFoundException("Cycle de tontine", cycleId));
        long collected = contributions.findAllByCycleId(cycleId).stream().mapToLong(Contribution::getPaidAmount).sum();
        cycle.setCollectedAmount(collected);
        cycle.setRemainingAmount(Math.max(0, cycle.getExpectedAmount() - collected));
        if (collected > 0 && collected < cycle.getExpectedAmount()) {
            cycle.setStatus(TontineCycleStatus.COLLECTION_IN_PROGRESS);
        } else if (collected >= cycle.getExpectedAmount()) {
            cycle.setStatus(TontineCycleStatus.READY_TO_PAY);
        }
        cycles.save(cycle);
    }
}
