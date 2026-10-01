package com.ticdiaspora.member.infrastructure.web;

import com.ticdiaspora.audit.infrastructure.web.AuditService;
import com.ticdiaspora.auth.infrastructure.UserEntity;
import com.ticdiaspora.auth.infrastructure.UserJpaRepository;
import com.ticdiaspora.member.infrastructure.persistence.MemberEntity;
import com.ticdiaspora.member.infrastructure.persistence.MemberJpaRepository;
import com.ticdiaspora.shared.domain.enums.AuditAction;
import com.ticdiaspora.shared.domain.enums.MemberStatus;
import com.ticdiaspora.shared.domain.exception.BusinessException;
import com.ticdiaspora.shared.domain.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class MemberService {

    private final MemberJpaRepository members;
    private final UserJpaRepository users;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public MemberService(MemberJpaRepository members, UserJpaRepository users, PasswordEncoder passwordEncoder, AuditService auditService) {
        this.members = members;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public Page<MemberEntity> list(Pageable pageable) {
        return members.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public MemberEntity get(UUID id) {
        return members.findById(id).orElseThrow(() -> new NotFoundException("Membre", id));
    }

    @Transactional
    public MemberEntity create(MemberDtos.MemberRequest request) {
        if (members.existsByEmailIgnoreCase(request.email())) {
            throw new BusinessException("MEMBER_EMAIL_ALREADY_EXISTS", "Un membre existe déjà avec cet email");
        }
        MemberEntity member = new MemberEntity();
        apply(request, member);
        member.setStatus(MemberStatus.ACTIVE);
        member = members.save(member);

        UserEntity user = new UserEntity();
        user.setMemberId(member.getId());
        user.setEmail(member.getEmail());
        user.setRole(member.getRole());
        user.setPasswordHash(passwordEncoder.encode(
                request.temporaryPassword() == null || request.temporaryPassword().isBlank()
                        ? "ChangeMe123!"
                        : request.temporaryPassword()));
        users.save(user);

        auditService.record(AuditAction.MEMBER_CREATED, "Member", member.getId(), null, member.getEmail(), "Création membre");
        return member;
    }

    @Transactional
    public MemberEntity update(UUID id, MemberDtos.MemberRequest request) {
        MemberEntity member = get(id);
        String oldEmail = member.getEmail();
        if (!oldEmail.equalsIgnoreCase(request.email()) && members.existsByEmailIgnoreCase(request.email())) {
            throw new BusinessException("MEMBER_EMAIL_ALREADY_EXISTS", "Un membre existe déjà avec cet email");
        }
        apply(request, member);
        users.findByEmailIgnoreCase(oldEmail).ifPresent(user -> {
            user.setEmail(member.getEmail());
            user.setRole(member.getRole());
        });
        auditService.record(AuditAction.MEMBER_UPDATED, "Member", member.getId(), oldEmail, member.getEmail(), "Modification membre");
        return member;
    }

    @Transactional
    public MemberEntity changeStatus(UUID id, MemberStatus status, AuditAction action, String reason) {
        MemberEntity member = get(id);
        MemberStatus oldStatus = member.getStatus();
        member.setStatus(status);
        users.findByEmailIgnoreCase(member.getEmail()).ifPresent(user -> user.setEnabled(status == MemberStatus.ACTIVE));
        auditService.record(action, "Member", member.getId(), oldStatus.name(), status.name(), reason);
        return member;
    }

    private void apply(MemberDtos.MemberRequest request, MemberEntity member) {
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
    }
}
