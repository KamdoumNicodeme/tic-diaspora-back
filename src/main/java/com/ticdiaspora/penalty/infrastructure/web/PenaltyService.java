package com.ticdiaspora.penalty.infrastructure.web;

import com.ticdiaspora.audit.infrastructure.web.AuditService;
import com.ticdiaspora.auth.application.CurrentUserService;
import com.ticdiaspora.charity.infrastructure.persistence.CharityFundMovementEntity;
import com.ticdiaspora.charity.infrastructure.persistence.CharityFundMovementJpaRepository;
import com.ticdiaspora.member.infrastructure.persistence.MemberEntity;
import com.ticdiaspora.member.infrastructure.persistence.MemberJpaRepository;
import com.ticdiaspora.notification.infrastructure.web.NotificationService;
import com.ticdiaspora.penalty.domain.PenaltyRules;
import com.ticdiaspora.penalty.infrastructure.persistence.PenaltyEntity;
import com.ticdiaspora.penalty.infrastructure.persistence.PenaltyJpaRepository;
import com.ticdiaspora.shared.domain.enums.AuditAction;
import com.ticdiaspora.shared.domain.enums.CharityMovementType;
import com.ticdiaspora.shared.domain.enums.NotificationType;
import com.ticdiaspora.shared.domain.enums.PenaltyStatus;
import com.ticdiaspora.shared.domain.enums.PenaltyType;
import com.ticdiaspora.shared.domain.exception.BusinessException;
import com.ticdiaspora.shared.domain.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class PenaltyService {

    private final PenaltyJpaRepository penalties;
    private final MemberJpaRepository members;
    private final CharityFundMovementJpaRepository charityMovements;
    private final PenaltyRules penaltyRules;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final CurrentUserService currentUser;

    public PenaltyService(
            PenaltyJpaRepository penalties,
            MemberJpaRepository members,
            CharityFundMovementJpaRepository charityMovements,
            PenaltyRules penaltyRules,
            AuditService auditService,
            NotificationService notificationService,
            CurrentUserService currentUser
    ) {
        this.penalties = penalties;
        this.members = members;
        this.charityMovements = charityMovements;
        this.penaltyRules = penaltyRules;
        this.auditService = auditService;
        this.notificationService = notificationService;
        this.currentUser = currentUser;
    }

    @Transactional(readOnly = true)
    public Page<PenaltyEntity> list(Pageable pageable) {
        return penalties.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public PenaltyEntity get(UUID id) {
        return penalties.findById(id).orElseThrow(() -> new NotFoundException("Pénalité", id));
    }

    @Transactional
    public PenaltyEntity create(UUID memberId, UUID meetingId, PenaltyType type, long amount, String reason) {
        MemberEntity member = members.findById(memberId).orElseThrow(() -> new NotFoundException("Membre", memberId));
        long penaltyAmount = amount > 0 ? amount : penaltyRules.amountFor(type);
        PenaltyEntity penalty = new PenaltyEntity();
        penalty.setMemberId(memberId);
        penalty.setMeetingId(meetingId);
        penalty.setType(type);
        penalty.setAmount(penaltyAmount);
        penalty.setReason(reason);
        penalty = penalties.save(penalty);
        member.setTotalPenaltiesAmount(member.getTotalPenaltiesAmount() + penaltyAmount);
        member.setUnpaidPenaltiesAmount(member.getUnpaidPenaltiesAmount() + penaltyAmount);
        notificationService.notifyInApp(memberId, NotificationType.PENALTY_CREATED,
                "Pénalité créée", "Une pénalité de " + penaltyAmount + " XAF a été créée.", "{\"penaltyId\":\"" + penalty.getId() + "\"}");
        return penalty;
    }

    @Transactional
    public PenaltyEntity pay(UUID id) {
        PenaltyEntity penalty = get(id);
        if (penalty.getStatus() != PenaltyStatus.PENDING) {
            throw new BusinessException("PENALTY_NOT_PAYABLE", "Seule une pénalité en attente peut être payée");
        }
        penalty.setStatus(PenaltyStatus.PAID);
        penalty.setPaidAt(Instant.now());
        penalty.setValidatedBy(currentUser.memberIdOrSystem());
        members.findById(penalty.getMemberId()).ifPresent(member ->
                member.setUnpaidPenaltiesAmount(Math.max(0, member.getUnpaidPenaltiesAmount() - penalty.getAmount())));

        CharityFundMovementEntity movement = new CharityFundMovementEntity();
        movement.setMemberId(penalty.getMemberId());
        movement.setPenaltyId(penalty.getId());
        movement.setType(CharityMovementType.PENALTY_INCOME);
        movement.setAmount(penalty.getAmount());
        movement.setDescription("Paiement pénalité " + penalty.getType());
        movement.setMovementDate(Instant.now());
        movement.setCreatedBy(currentUser.memberIdOrSystem());
        charityMovements.save(movement);
        return penalty;
    }

    @Transactional
    public PenaltyEntity waive(UUID id, String reason) {
        penaltyRules.verifyCancellationReason(reason);
        PenaltyEntity penalty = get(id);
        if (penalty.getStatus() != PenaltyStatus.PENDING) {
            throw new BusinessException("PENALTY_NOT_WAIVABLE", "Seule une pénalité en attente peut être annulée");
        }
        penalty.setStatus(PenaltyStatus.WAIVED);
        penalty.setCancellationReason(reason);
        members.findById(penalty.getMemberId()).ifPresent(member ->
                member.setUnpaidPenaltiesAmount(Math.max(0, member.getUnpaidPenaltiesAmount() - penalty.getAmount())));
        auditService.record(AuditAction.PENALTY_WAIVED, "Penalty", penalty.getId(),
                PenaltyStatus.PENDING.name(), PenaltyStatus.WAIVED.name(), reason);
        return penalty;
    }
}
