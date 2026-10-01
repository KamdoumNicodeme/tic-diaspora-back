package com.ticdiaspora.application.usecase;
import com.ticdiaspora.application.port.in.*;
import com.ticdiaspora.application.port.out.*;

import com.ticdiaspora.domain.model.BeneficiaryConfirmation;
import com.ticdiaspora.application.port.out.BeneficiaryConfirmationRepositoryPort;
import com.ticdiaspora.domain.model.Contribution;
import com.ticdiaspora.application.port.out.ContributionRepositoryPort;
import com.ticdiaspora.domain.model.Member;
import com.ticdiaspora.application.port.out.MemberRepositoryPort;
import com.ticdiaspora.domain.model.enums.AuditAction;
import com.ticdiaspora.domain.model.enums.BeneficiaryConfirmationStatus;
import com.ticdiaspora.domain.model.enums.ContributionStatus;
import com.ticdiaspora.domain.model.enums.ContributionType;
import com.ticdiaspora.domain.model.enums.MemberStatus;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@UseCase
public class TontineCycleService {

    private final TontineCycleRepositoryPort cycles;
    private final MemberRepositoryPort members;
    private final ContributionRepositoryPort contributions;
    private final BeneficiaryConfirmationRepositoryPort beneficiaryConfirmations;
    private final TontineRules tontineRules;
    private final AuditService auditService;

    public TontineCycleService(
            TontineCycleRepositoryPort cycles,
            MemberRepositoryPort members,
            ContributionRepositoryPort contributions,
            BeneficiaryConfirmationRepositoryPort beneficiaryConfirmations,
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
    public Page<TontineCycle> list(Pageable pageable) {
        return cycles.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public TontineCycle get(UUID id) {
        return cycles.findById(id).orElseThrow(() -> new NotFoundException("Cycle de tontine", id));
    }

    @Transactional
    public TontineCycle create(TontineCycleCommand request) {
        cycles.findByMonthAndYear(request.month(), request.year()).ifPresent(existing -> {
            throw new BusinessException("TONTINE_CYCLE_ALREADY_EXISTS", "Un cycle existe déjà pour ce mois");
        });
        List<Member> activeMembers = members.findAllByStatus(MemberStatus.ACTIVE);
        if (activeMembers.isEmpty()) {
            throw new BusinessException("NO_ACTIVE_MEMBERS", "Impossible de créer un cycle sans membre actif");
        }
        UUID beneficiaryId = request.beneficiaryId() != null ? request.beneficiaryId() : proposeBeneficiary(activeMembers);
        BeneficiarySelection selection = resolveBeneficiarySelection(
                beneficiaryId,
                request.secondaryBeneficiaryId(),
                activeMembers,
                request.beneficiaryId() == null
        );
        long expectedAmount = tontineRules.expectedAmount(activeMembers.stream()
                .map(member -> member.getContributionType().monthlyAmount())
                .toList());

        TontineCycle cycle = new TontineCycle();
        cycle.setMonth(request.month());
        cycle.setYear(request.year());
        cycle.setBeneficiaryId(selection.primary().getId());
        cycle.setSecondaryBeneficiaryId(selection.secondary() == null ? null : selection.secondary().getId());
        cycle.setStatus(TontineCycleStatus.PLANNED);
        cycle.setExpectedAmount(expectedAmount);
        cycle.setRemainingAmount(expectedAmount);
        cycle.setComment(request.comment());
        cycle = cycles.save(cycle);

        for (Member member : activeMembers) {
            Contribution contribution = new Contribution();
            contribution.setCycleId(cycle.getId());
            contribution.setMemberId(member.getId());
            contribution.setExpectedAmount(member.getContributionType().monthlyAmount());
            contribution.setPaidAmount(0);
            contribution.setCurrency("XAF");
            contribution.setStatus(ContributionStatus.PENDING);
            contributions.save(contribution);
        }

        BeneficiaryConfirmation confirmation = new BeneficiaryConfirmation();
        confirmation.setCycleId(cycle.getId());
        confirmation.setBeneficiaryId(selection.primary().getId());
        confirmation.setExpectedAmount(expectedAmount);
        confirmation.setReceivedAmount(0);
        confirmation.setStatus(BeneficiaryConfirmationStatus.PENDING);
        beneficiaryConfirmations.save(confirmation);
        return cycle;
    }

    @Transactional
    public TontineCycle open(UUID id) {
        TontineCycle cycle = get(id);
        cycle.setStatus(TontineCycleStatus.OPEN);
        cycle.setOpenedAt(Instant.now());
        return cycles.save(cycle);
    }

    @Transactional
    public TontineCycle changeBeneficiary(UUID id, UUID beneficiaryId, String reason) {
        return changeBeneficiary(id, beneficiaryId, null, true, reason);
    }

    @Transactional
    public TontineCycle changeBeneficiary(UUID id, UUID beneficiaryId, UUID secondaryBeneficiaryId, boolean unanimousAgreement, String reason) {
        if (!unanimousAgreement) {
            throw new BusinessException("BENEFICIARY_SWITCH_REQUIRES_UNANIMITY", "Le changement de bénéficiaire doit être validé après discussion et accord à l'unanimité");
        }
        TontineCycle cycle = get(id);
        List<Member> activeMembers = members.findAllByStatus(MemberStatus.ACTIVE);
        BeneficiarySelection selection = resolveBeneficiarySelection(beneficiaryId, secondaryBeneficiaryId, activeMembers, false);
        String old = cycle.getBeneficiaryId() + "/" + cycle.getSecondaryBeneficiaryId();
        cycle.setBeneficiaryId(selection.primary().getId());
        cycle.setSecondaryBeneficiaryId(selection.secondary() == null ? null : selection.secondary().getId());
        cycle = cycles.save(cycle);
        beneficiaryConfirmations.findByCycleId(id).ifPresent(confirmation -> {
            confirmation.setBeneficiaryId(selection.primary().getId());
            beneficiaryConfirmations.save(confirmation);
        });
        String updated = cycle.getBeneficiaryId() + "/" + cycle.getSecondaryBeneficiaryId();
        auditService.record(AuditAction.MEMBER_UPDATED, "TontineCycle", id, old, updated, reason);
        return cycle;
    }

    @Transactional
    public TontineCycle close(UUID id) {
        TontineCycle cycle = get(id);
        long missing = contributions.countByCycleIdAndStatusNot(id, ContributionStatus.PAID);
        boolean beneficiaryConfirmed = beneficiaryConfirmations.findByCycleId(id)
                .map(confirmation -> confirmation.getStatus() == BeneficiaryConfirmationStatus.FULLY_RECEIVED)
                .orElse(false);
        tontineRules.verifyClosable(missing, beneficiaryConfirmed);
        cycle.setStatus(TontineCycleStatus.CLOSED);
        cycle.setClosedAt(Instant.now());
        cycle = cycles.save(cycle);
        auditService.record(AuditAction.TONTINE_CYCLE_CLOSED, "TontineCycle", id, null, TontineCycleStatus.CLOSED.name(), "Cycle clôturé");
        return cycle;
    }

    public UUID proposeBeneficiary(List<Member> activeMembers) {
        List<TontineCycle> allCycles = cycles.findAll();
        Map<UUID, Long> beneficiaryCounts = beneficiaryBenefitUnits(allCycles, activeMembers);
        Map<UUID, TontineCycle> latestCyclesByBeneficiary = latestCyclesByBeneficiary(allCycles);
        return activeMembers.stream()
                .filter(member -> isEligiblePrimaryBeneficiary(member, activeMembers))
                .min(Comparator
                        .comparingLong((Member member) -> beneficiaryCounts.getOrDefault(member.getId(), 0L))
                        .thenComparing(member -> {
                            TontineCycle latest = latestCyclesByBeneficiary.get(member.getId());
                            return latest == null ? 0 : latest.getYear() * 100 + latest.getMonth();
                        })
                        .thenComparing(Member::getJoinedAt)
                        .thenComparing(Member::getId))
                .map(Member::getId)
                .orElseThrow(() -> new BusinessException("NO_ELIGIBLE_BENEFICIARY", "Aucun bénéficiaire actif éligible selon les cotisations disponibles"));
    }

    private BeneficiarySelection resolveBeneficiarySelection(
            UUID beneficiaryId,
            UUID secondaryBeneficiaryId,
            List<Member> activeMembers,
            boolean allowAutomaticSecondarySelection
    ) {
        Member primary = ensureActiveMember(beneficiaryId);
        if (primary.getContributionType() == ContributionType.XAF_100000) {
            if (secondaryBeneficiaryId != null) {
                throw new BusinessException("SECONDARY_BENEFICIARY_NOT_ALLOWED", "Un bénéficiaire à 100000 XAF bouffe seul pour le cycle");
            }
            return new BeneficiarySelection(primary, null);
        }

        if (secondaryBeneficiaryId == null && !allowAutomaticSecondarySelection) {
            throw new BusinessException("SECOND_50000_BENEFICIARY_REQUIRED", "Quand le bénéficiaire cotise à 50000 XAF, il faut choisir un deuxième bénéficiaire actif cotisant aussi à 50000 XAF");
        }
        Member secondary = secondaryBeneficiaryId == null ? proposeSecondaryBeneficiary(primary, activeMembers) : ensureActiveMember(secondaryBeneficiaryId);
        if (primary.getId().equals(secondary.getId())) {
            throw new BusinessException("BENEFICIARIES_MUST_BE_DISTINCT", "Les deux bénéficiaires 50000 XAF doivent être deux membres différents");
        }
        if (secondary.getContributionType() != ContributionType.XAF_50000) {
            throw new BusinessException("SECONDARY_BENEFICIARY_MUST_BE_50000", "Le deuxième bénéficiaire doit cotiser à 50000 XAF");
        }
        return new BeneficiarySelection(primary, secondary);
    }

    private boolean isEligiblePrimaryBeneficiary(Member member, List<Member> activeMembers) {
        if (member.getContributionType() == ContributionType.XAF_100000) {
            return true;
        }
        long activeFiftyThousandMembers = activeMembers.stream()
                .filter(activeMember -> activeMember.getContributionType() == ContributionType.XAF_50000)
                .count();
        return activeFiftyThousandMembers >= 2;
    }

    private Member proposeSecondaryBeneficiary(Member primary, List<Member> activeMembers) {
        List<TontineCycle> allCycles = cycles.findAll();
        Map<UUID, Long> beneficiaryCounts = beneficiaryBenefitUnits(allCycles, activeMembers);
        Map<UUID, TontineCycle> latestCyclesByBeneficiary = latestCyclesByBeneficiary(allCycles);
        return activeMembers.stream()
                .filter(member -> !member.getId().equals(primary.getId()))
                .filter(member -> member.getContributionType() == ContributionType.XAF_50000)
                .min(Comparator
                        .comparingLong((Member member) -> beneficiaryCounts.getOrDefault(member.getId(), 0L))
                        .thenComparing(member -> {
                            TontineCycle latest = latestCyclesByBeneficiary.get(member.getId());
                            return latest == null ? 0 : latest.getYear() * 100 + latest.getMonth();
                        })
                        .thenComparing(Member::getJoinedAt)
                        .thenComparing(Member::getId))
                .orElseThrow(() -> new BusinessException("SECOND_50000_BENEFICIARY_REQUIRED", "Il faut deux membres actifs cotisant à 50000 XAF pour ce cycle"));
    }

    private Map<UUID, Long> beneficiaryBenefitUnits(List<TontineCycle> allCycles, List<Member> activeMembers) {
        Map<UUID, Member> activeMembersById = activeMembers.stream()
                .collect(Collectors.toMap(Member::getId, Function.identity()));
        Map<UUID, Long> benefitUnits = new HashMap<>();
        for (TontineCycle cycle : allCycles) {
            if (cycle.getBeneficiaryId() != null) {
                benefitUnits.merge(cycle.getBeneficiaryId(), primaryBenefitUnits(cycle, activeMembersById), Long::sum);
            }
            if (cycle.getSecondaryBeneficiaryId() != null) {
                benefitUnits.merge(cycle.getSecondaryBeneficiaryId(), 1L, Long::sum);
            }
        }
        return benefitUnits;
    }

    private long primaryBenefitUnits(TontineCycle cycle, Map<UUID, Member> activeMembersById) {
        if (cycle.getSecondaryBeneficiaryId() != null) {
            return 1L;
        }
        Member beneficiary = activeMembersById.get(cycle.getBeneficiaryId());
        return beneficiary != null && beneficiary.getContributionType() == ContributionType.XAF_100000 ? 2L : 1L;
    }

    private Map<UUID, TontineCycle> latestCyclesByBeneficiary(List<TontineCycle> allCycles) {
        return allCycles.stream()
                .flatMap(cycle -> java.util.stream.Stream.of(
                        new BeneficiaryCycle(cycle.getBeneficiaryId(), cycle),
                        new BeneficiaryCycle(cycle.getSecondaryBeneficiaryId(), cycle)
                ))
                .filter(entry -> entry.memberId() != null)
                .collect(Collectors.toMap(
                        BeneficiaryCycle::memberId,
                        BeneficiaryCycle::cycle,
                        (left, right) -> left.getYear() > right.getYear()
                                || (left.getYear() == right.getYear() && left.getMonth() >= right.getMonth())
                                ? left
                                : right
                ));
    }

    private Member ensureActiveMember(UUID memberId) {
        Member member = members.findById(memberId).orElseThrow(() -> new NotFoundException("Membre", memberId));
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new BusinessException("BENEFICIARY_NOT_ACTIVE", "Le bénéficiaire doit être un membre actif");
        }
        return member;
    }

    private record BeneficiarySelection(Member primary, Member secondary) {
    }

    private record BeneficiaryCycle(UUID memberId, TontineCycle cycle) {
    }
}
