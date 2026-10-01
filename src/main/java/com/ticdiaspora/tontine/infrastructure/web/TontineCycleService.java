package com.ticdiaspora.tontine.infrastructure.web;

import com.ticdiaspora.audit.infrastructure.web.AuditService;
import com.ticdiaspora.beneficiary.infrastructure.persistence.BeneficiaryConfirmationEntity;
import com.ticdiaspora.beneficiary.infrastructure.persistence.BeneficiaryConfirmationJpaRepository;
import com.ticdiaspora.contribution.infrastructure.persistence.ContributionEntity;
import com.ticdiaspora.contribution.infrastructure.persistence.ContributionJpaRepository;
import com.ticdiaspora.member.infrastructure.persistence.MemberEntity;
import com.ticdiaspora.member.infrastructure.persistence.MemberJpaRepository;
import com.ticdiaspora.shared.domain.enums.AuditAction;
import com.ticdiaspora.shared.domain.enums.BeneficiaryConfirmationStatus;
import com.ticdiaspora.shared.domain.enums.ContributionStatus;
import com.ticdiaspora.shared.domain.enums.MemberStatus;
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
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class TontineCycleService {

    private final TontineCycleJpaRepository cycles;
    private final MemberJpaRepository members;
    private final ContributionJpaRepository contributions;
    private final BeneficiaryConfirmationJpaRepository beneficiaryConfirmations;
    private final TontineRules tontineRules;
    private final AuditService auditService;

    public TontineCycleService(
            TontineCycleJpaRepository cycles,
            MemberJpaRepository members,
            ContributionJpaRepository contributions,
            BeneficiaryConfirmationJpaRepository beneficiaryConfirmations,
            TontineRules tontineRules,
            AuditService auditService
    ) {
        this.cycles = cycles;
        this.members = members;
        this.contributions = contributions;
        this.beneficiaryConfirmations = beneficiaryConfirmations;
        this.tontineRules = tontineRules;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public Page<TontineCycleEntity> list(Pageable pageable) {
        return cycles.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public TontineCycleEntity get(UUID id) {
        return cycles.findById(id).orElseThrow(() -> new NotFoundException("Cycle de tontine", id));
    }

    @Transactional
    public TontineCycleEntity create(TontineDtos.TontineCycleRequest request) {
        cycles.findByMonthAndYear(request.month(), request.year()).ifPresent(existing -> {
            throw new BusinessException("TONTINE_CYCLE_ALREADY_EXISTS", "Un cycle existe déjà pour ce mois");
        });
        List<MemberEntity> activeMembers = members.findAllByStatus(MemberStatus.ACTIVE);
        if (activeMembers.isEmpty()) {
            throw new BusinessException("NO_ACTIVE_MEMBERS", "Impossible de créer un cycle sans membre actif");
        }
        UUID beneficiaryId = request.beneficiaryId() != null ? request.beneficiaryId() : proposeBeneficiary(activeMembers);
        ensureActiveMember(beneficiaryId);
        long expectedAmount = tontineRules.expectedAmount(activeMembers.stream()
                .map(member -> member.getContributionType().monthlyAmount())
                .toList());

        TontineCycleEntity cycle = new TontineCycleEntity();
        cycle.setMonth(request.month());
        cycle.setYear(request.year());
        cycle.setBeneficiaryId(beneficiaryId);
        cycle.setExpectedAmount(expectedAmount);
        cycle.setRemainingAmount(expectedAmount);
        cycle.setComment(request.comment());
        cycle = cycles.save(cycle);

        for (MemberEntity member : activeMembers) {
            ContributionEntity contribution = new ContributionEntity();
            contribution.setCycleId(cycle.getId());
            contribution.setMemberId(member.getId());
            contribution.setExpectedAmount(member.getContributionType().monthlyAmount());
            contribution.setPaidAmount(0);
            contribution.setStatus(ContributionStatus.PENDING);
            contributions.save(contribution);
        }

        BeneficiaryConfirmationEntity confirmation = new BeneficiaryConfirmationEntity();
        confirmation.setCycleId(cycle.getId());
        confirmation.setBeneficiaryId(beneficiaryId);
        confirmation.setExpectedAmount(expectedAmount);
        confirmation.setReceivedAmount(0);
        beneficiaryConfirmations.save(confirmation);
        return cycle;
    }

    @Transactional
    public TontineCycleEntity open(UUID id) {
        TontineCycleEntity cycle = get(id);
        cycle.setStatus(TontineCycleStatus.OPEN);
        cycle.setOpenedAt(Instant.now());
        return cycle;
    }

    @Transactional
    public TontineCycleEntity changeBeneficiary(UUID id, UUID beneficiaryId, String reason) {
        TontineCycleEntity cycle = get(id);
        ensureActiveMember(beneficiaryId);
        UUID old = cycle.getBeneficiaryId();
        cycle.setBeneficiaryId(beneficiaryId);
        beneficiaryConfirmations.findByCycleId(id).ifPresent(confirmation -> confirmation.setBeneficiaryId(beneficiaryId));
        auditService.record(AuditAction.MEMBER_UPDATED, "TontineCycle", id, String.valueOf(old), String.valueOf(beneficiaryId), reason);
        return cycle;
    }

    @Transactional
    public TontineCycleEntity close(UUID id) {
        TontineCycleEntity cycle = get(id);
        long missing = contributions.countByCycleIdAndStatusNot(id, ContributionStatus.PAID);
        boolean beneficiaryConfirmed = beneficiaryConfirmations.findByCycleId(id)
                .map(confirmation -> confirmation.getStatus() == BeneficiaryConfirmationStatus.FULLY_RECEIVED)
                .orElse(false);
        tontineRules.verifyClosable(missing, beneficiaryConfirmed);
        cycle.setStatus(TontineCycleStatus.CLOSED);
        cycle.setClosedAt(Instant.now());
        auditService.record(AuditAction.TONTINE_CYCLE_CLOSED, "TontineCycle", id, null, TontineCycleStatus.CLOSED.name(), "Cycle clôturé");
        return cycle;
    }

    public UUID proposeBeneficiary(List<MemberEntity> activeMembers) {
        return activeMembers.stream()
                .min(Comparator.comparing(MemberEntity::getJoinedAt).thenComparing(MemberEntity::getId))
                .map(MemberEntity::getId)
                .orElseThrow();
    }

    private void ensureActiveMember(UUID memberId) {
        MemberEntity member = members.findById(memberId).orElseThrow(() -> new NotFoundException("Membre", memberId));
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new BusinessException("BENEFICIARY_NOT_ACTIVE", "Le bénéficiaire doit être un membre actif");
        }
    }
}
