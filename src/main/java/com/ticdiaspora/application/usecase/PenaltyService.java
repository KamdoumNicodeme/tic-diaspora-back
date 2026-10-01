package com.ticdiaspora.application.usecase;
import com.ticdiaspora.application.port.in.*;
import com.ticdiaspora.application.port.out.*;

import com.ticdiaspora.application.port.out.CurrentUserPort;
import com.ticdiaspora.domain.model.CharityFundMovement;
import com.ticdiaspora.application.port.out.CharityFundMovementRepositoryPort;
import com.ticdiaspora.domain.model.Member;
import com.ticdiaspora.application.port.out.MemberRepositoryPort;
import com.ticdiaspora.domain.service.PenaltyRules;
import com.ticdiaspora.domain.model.Penalty;
import com.ticdiaspora.application.port.out.PenaltyRepositoryPort;
import com.ticdiaspora.domain.model.enums.AuditAction;
import com.ticdiaspora.domain.model.enums.CharityMovementType;
import com.ticdiaspora.domain.model.enums.NotificationType;
import com.ticdiaspora.domain.model.enums.PenaltyStatus;
import com.ticdiaspora.domain.model.enums.PenaltyType;
import com.ticdiaspora.domain.exception.BusinessException;
import com.ticdiaspora.domain.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.ticdiaspora.application.annotation.UseCase;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@UseCase
public class PenaltyService {

    private final PenaltyRepositoryPort penalties;
    private final MemberRepositoryPort members;
    private final CharityFundMovementRepositoryPort charityMovements;
    private final PenaltyRules penaltyRules;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final CurrentUserPort currentUser;

    public PenaltyService(
            PenaltyRepositoryPort penalties,
            MemberRepositoryPort members,
            CharityFundMovementRepositoryPort charityMovements,
            PenaltyRules penaltyRules,
            AuditService auditService,
            NotificationService notificationService,
            CurrentUserPort currentUser
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
    public Page<Penalty> list(Pageable pageable) {
        return penalties.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Penalty get(UUID id) {
        return penalties.findById(id).orElseThrow(() -> new NotFoundException("Pénalité", id));
    }

    @Transactional
    public Penalty create(UUID memberId, UUID meetingId, PenaltyType type, long amount, String reason) {
        Member member = members.findById(memberId).orElseThrow(() -> new NotFoundException("Membre", memberId));
        long penaltyAmount = amount > 0 ? amount : penaltyRules.amountFor(type);
        Penalty penalty = new Penalty();
        penalty.setMemberId(memberId);
        penalty.setMeetingId(meetingId);
        penalty.setType(type);
        penalty.setAmount(penaltyAmount);
        penalty.setStatus(PenaltyStatus.PENDING);
        penalty.setReason(reason);
        penalty = penalties.save(penalty);
        member.setTotalPenaltiesAmount(member.getTotalPenaltiesAmount() + penaltyAmount);
        member.setUnpaidPenaltiesAmount(member.getUnpaidPenaltiesAmount() + penaltyAmount);
        members.save(member);
        notificationService.notifyInApp(memberId, NotificationType.PENALTY_CREATED,
                "Pénalité créée", "Une pénalité de " + penaltyAmount + " XAF a été créée.", "{\"penaltyId\":\"" + penalty.getId() + "\"}");
        return penalty;
    }

    @Transactional
    public Penalty pay(UUID id) {
        Penalty penalty = get(id);
        if (penalty.getStatus() != PenaltyStatus.PENDING) {
            throw new BusinessException("PENALTY_NOT_PAYABLE", "Seule une pénalité en attente peut être payée");
        }
        penalty.setStatus(PenaltyStatus.PAID);
        penalty.setPaidAt(Instant.now());
        penalty.setValidatedBy(currentUser.memberIdOrSystem());
        long paidAmount = penalty.getAmount();
        members.findById(penalty.getMemberId()).ifPresent(member -> {
            member.setUnpaidPenaltiesAmount(Math.max(0, member.getUnpaidPenaltiesAmount() - paidAmount));
            members.save(member);
        });
        penalty = penalties.save(penalty);

        CharityFundMovement movement = new CharityFundMovement();
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
    public Penalty waive(UUID id, String reason) {
        penaltyRules.verifyCancellationReason(reason);
        Penalty penalty = get(id);
        if (penalty.getStatus() != PenaltyStatus.PENDING) {
            throw new BusinessException("PENALTY_NOT_WAIVABLE", "Seule une pénalité en attente peut être annulée");
        }
        penalty.setStatus(PenaltyStatus.WAIVED);
        penalty.setCancellationReason(reason);
        long waivedAmount = penalty.getAmount();
        members.findById(penalty.getMemberId()).ifPresent(member -> {
            member.setUnpaidPenaltiesAmount(Math.max(0, member.getUnpaidPenaltiesAmount() - waivedAmount));
            members.save(member);
        });
        penalty = penalties.save(penalty);
        auditService.record(AuditAction.PENALTY_WAIVED, "Penalty", penalty.getId(),
                PenaltyStatus.PENDING.name(), PenaltyStatus.WAIVED.name(), reason);
        return penalty;
    }
}
