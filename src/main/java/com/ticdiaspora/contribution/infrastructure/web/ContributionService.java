package com.ticdiaspora.contribution.infrastructure.web;

import com.ticdiaspora.audit.infrastructure.web.AuditService;
import com.ticdiaspora.auth.application.CurrentUserService;
import com.ticdiaspora.contribution.infrastructure.persistence.ContributionEntity;
import com.ticdiaspora.contribution.infrastructure.persistence.ContributionJpaRepository;
import com.ticdiaspora.contribution.infrastructure.persistence.ContributionPaymentEntity;
import com.ticdiaspora.contribution.infrastructure.persistence.ContributionPaymentJpaRepository;
import com.ticdiaspora.notification.infrastructure.web.NotificationService;
import com.ticdiaspora.shared.domain.enums.AuditAction;
import com.ticdiaspora.shared.domain.enums.NotificationType;
import com.ticdiaspora.shared.domain.enums.PaymentMethod;
import com.ticdiaspora.shared.domain.enums.TontineCycleStatus;
import com.ticdiaspora.shared.domain.exception.BusinessException;
import com.ticdiaspora.shared.domain.exception.NotFoundException;
import com.ticdiaspora.tontine.domain.TontineRules;
import com.ticdiaspora.tontine.infrastructure.persistence.TontineCycleEntity;
import com.ticdiaspora.tontine.infrastructure.persistence.TontineCycleJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class ContributionService {

    private final ContributionJpaRepository contributions;
    private final ContributionPaymentJpaRepository payments;
    private final TontineCycleJpaRepository cycles;
    private final TontineRules tontineRules;
    private final CurrentUserService currentUser;
    private final AuditService auditService;
    private final NotificationService notificationService;

    public ContributionService(
            ContributionJpaRepository contributions,
            ContributionPaymentJpaRepository payments,
            TontineCycleJpaRepository cycles,
            TontineRules tontineRules,
            CurrentUserService currentUser,
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
    public Page<ContributionEntity> list(Pageable pageable) {
        return contributions.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public ContributionEntity get(UUID id) {
        return contributions.findById(id).orElseThrow(() -> new NotFoundException("Cotisation", id));
    }

    @Transactional
    public ContributionEntity recordPayment(UUID cycleId, UUID memberId, long amount, PaymentMethod method, String reference, String proofUrl) {
        ContributionEntity contribution = contributions.findByCycleIdAndMemberId(cycleId, memberId)
                .orElseThrow(() -> new NotFoundException("Cotisation du membre dans le cycle", memberId));
        if (contribution.getStatus().name().equals("CANCELLED")) {
            throw new BusinessException("CONTRIBUTION_CANCELLED", "Une cotisation annulée ne peut pas être payée");
        }
        ContributionPaymentEntity payment = new ContributionPaymentEntity();
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
        refreshCycleAmounts(cycleId);
        return contribution;
    }

    @Transactional
    public ContributionEntity validate(UUID id) {
        ContributionEntity contribution = get(id);
        contribution.setValidatedBy(currentUser.memberIdOrSystem());
        contribution.setValidatedAt(Instant.now());
        auditService.record(AuditAction.CONTRIBUTION_VALIDATED, "Contribution", contribution.getId(), null,
                contribution.getStatus().name(), "Validation cotisation");
        notificationService.notifyInApp(contribution.getMemberId(), NotificationType.CONTRIBUTION_VALIDATED,
                "Cotisation validée", "Votre cotisation a été validée.", "{\"contributionId\":\"" + contribution.getId() + "\"}");
        refreshCycleAmounts(contribution.getCycleId());
        return contribution;
    }

    private void refreshCycleAmounts(UUID cycleId) {
        TontineCycleEntity cycle = cycles.findById(cycleId).orElseThrow(() -> new NotFoundException("Cycle de tontine", cycleId));
        long collected = contributions.findAllByCycleId(cycleId).stream().mapToLong(ContributionEntity::getPaidAmount).sum();
        cycle.setCollectedAmount(collected);
        cycle.setRemainingAmount(Math.max(0, cycle.getExpectedAmount() - collected));
        if (collected > 0 && collected < cycle.getExpectedAmount()) {
            cycle.setStatus(TontineCycleStatus.COLLECTION_IN_PROGRESS);
        } else if (collected >= cycle.getExpectedAmount()) {
            cycle.setStatus(TontineCycleStatus.READY_TO_PAY);
        }
    }
}
