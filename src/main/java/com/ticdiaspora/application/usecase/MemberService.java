package com.ticdiaspora.application.usecase;
import com.ticdiaspora.application.port.in.*;
import com.ticdiaspora.application.port.out.*;

import com.ticdiaspora.domain.model.User;
import com.ticdiaspora.application.port.out.UserRepositoryPort;
import com.ticdiaspora.domain.model.Member;
import com.ticdiaspora.application.port.out.MemberRepositoryPort;
import com.ticdiaspora.domain.model.enums.AuditAction;
import com.ticdiaspora.domain.model.enums.MemberStatus;
import com.ticdiaspora.domain.exception.BusinessException;
import com.ticdiaspora.domain.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.ticdiaspora.application.annotation.UseCase;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@UseCase
public class MemberService {

    private final MemberRepositoryPort members;
    private final UserRepositoryPort users;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public MemberService(MemberRepositoryPort members, UserRepositoryPort users, PasswordEncoder passwordEncoder, AuditService auditService) {
        this.members = members;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public Page<Member> list(Pageable pageable) {
        return members.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Member get(UUID id) {
        return members.findById(id).orElseThrow(() -> new NotFoundException("Membre", id));
    }

    @Transactional
    public Member create(MemberCommand request) {
        if (members.existsByEmailIgnoreCase(request.email())) {
            throw new BusinessException("MEMBER_EMAIL_ALREADY_EXISTS", "Un membre existe déjà avec cet email");
        }
        Member member = new Member();
        apply(request, member);
        member.setStatus(MemberStatus.ACTIVE);
        member = members.save(member);

        User user = new User();
        user.setMemberId(member.getId());
        user.setEmail(member.getEmail());
        user.setRole(member.getRole());
        user.setEnabled(true);
        user.setPasswordHash(passwordEncoder.encode(
                request.temporaryPassword() == null || request.temporaryPassword().isBlank()
                        ? "ChangeMe123!"
                        : request.temporaryPassword()));
        users.save(user);

        auditService.record(AuditAction.MEMBER_CREATED, "Member", member.getId(), null, member.getEmail(), "Création membre");
        return member;
    }

    @Transactional
    public Member update(UUID id, MemberCommand request) {
        Member member = get(id);
        String oldEmail = member.getEmail();
        if (!oldEmail.equalsIgnoreCase(request.email()) && members.existsByEmailIgnoreCase(request.email())) {
            throw new BusinessException("MEMBER_EMAIL_ALREADY_EXISTS", "Un membre existe déjà avec cet email");
        }
        apply(request, member);
        member = members.save(member);
        Member savedMember = member;
        users.findByEmailIgnoreCase(oldEmail).ifPresent(user -> {
            user.setEmail(savedMember.getEmail());
            user.setRole(savedMember.getRole());
            users.save(user);
        });
        auditService.record(AuditAction.MEMBER_UPDATED, "Member", member.getId(), oldEmail, member.getEmail(), "Modification membre");
        return member;
    }

    @Transactional
    public Member updateProfile(UUID id, MemberProfileCommand request) {
        Member member = get(id);
        String oldName = member.getFirstName() + " " + member.getLastName();
        member.setFirstName(request.firstName());
        member.setLastName(request.lastName());
        member.setPhone(request.phone());
        member.setCountry(request.country());
        member.setCity(request.city());
        member.setFullAddress(request.fullAddress());
        member.setPhotoUrl(request.photoUrl());
        member.setEmergencyContactName(request.emergencyContactName());
        member.setEmergencyContactPhone(request.emergencyContactPhone());
        member.setOrangeMoneyNumber(request.orangeMoneyNumber());
        member.setOrangeMoneyAccountName(request.orangeMoneyAccountName());
        member.setMtnMoneyNumber(request.mtnMoneyNumber());
        member.setMtnMoneyAccountName(request.mtnMoneyAccountName());
        member = members.save(member);
        auditService.record(AuditAction.MEMBER_UPDATED, "Member", member.getId(), oldName,
                member.getFirstName() + " " + member.getLastName(), "Modification profil membre");
        return member;
    }

    @Transactional
    public Member changeStatus(UUID id, MemberStatus status, AuditAction action, String reason) {
        Member member = get(id);
        MemberStatus oldStatus = member.getStatus();
        member.setStatus(status);
        member = members.save(member);
        users.findByEmailIgnoreCase(member.getEmail()).ifPresent(user -> {
            user.setEnabled(status == MemberStatus.ACTIVE);
            users.save(user);
        });
        auditService.record(action, "Member", member.getId(), oldStatus.name(), status.name(), reason);
        return member;
    }

    private void apply(MemberCommand request, Member member) {
        member.setFirstName(request.firstName());
        member.setLastName(request.lastName());
        member.setEmail(request.email());
        member.setPhone(request.phone());
        member.setCountry(request.country());
        member.setCity(request.city());
        member.setFullAddress(request.fullAddress());
        member.setJoinedAt(request.joinedAt());
        member.setRole(request.role());
        member.setContributionType(request.contributionType());
        member.setPhotoUrl(request.photoUrl());
        member.setEmergencyContactName(request.emergencyContactName());
        member.setEmergencyContactPhone(request.emergencyContactPhone());
        member.setOrangeMoneyNumber(request.orangeMoneyNumber());
        member.setOrangeMoneyAccountName(request.orangeMoneyAccountName());
        member.setMtnMoneyNumber(request.mtnMoneyNumber());
        member.setMtnMoneyAccountName(request.mtnMoneyAccountName());
    }
}
