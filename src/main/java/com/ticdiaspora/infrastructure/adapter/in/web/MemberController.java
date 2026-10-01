package com.ticdiaspora.infrastructure.adapter.in.web;
import com.ticdiaspora.infrastructure.adapter.in.web.mapper.AttendanceWebMapper;
import com.ticdiaspora.infrastructure.adapter.in.web.mapper.ContributionWebMapper;
import com.ticdiaspora.infrastructure.adapter.in.web.mapper.MemberWebMapper;
import com.ticdiaspora.infrastructure.adapter.in.web.mapper.PenaltyWebMapper;

import com.ticdiaspora.application.usecase.*;
import com.ticdiaspora.application.port.out.*;
import com.ticdiaspora.infrastructure.adapter.in.web.dto.*;

import com.ticdiaspora.application.port.out.AttendanceRecordRepositoryPort;
import com.ticdiaspora.infrastructure.adapter.in.web.dto.AttendanceDtos;
import com.ticdiaspora.infrastructure.adapter.in.web.dto.ContributionDtos;
import com.ticdiaspora.domain.exception.BusinessException;
import com.ticdiaspora.domain.model.Contribution;
import com.ticdiaspora.application.port.out.MeetingChairpersonHistoryRepositoryPort;
import com.ticdiaspora.domain.model.Member;
import com.ticdiaspora.application.port.out.PenaltyRepositoryPort;
import com.ticdiaspora.infrastructure.adapter.in.web.dto.PenaltyDtos;
import com.ticdiaspora.domain.model.enums.AuditAction;
import com.ticdiaspora.domain.model.enums.MemberStatus;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;
    private final ContributionService contributionService;
    private final AttendanceRecordRepositoryPort attendanceRecords;
    private final PenaltyRepositoryPort penalties;
    private final MeetingChairpersonHistoryRepositoryPort chairHistory;
    private final CurrentUserPort currentUser;
    private final MemberWebMapper memberMapper;
    private final ContributionWebMapper contributionMapper;
    private final AttendanceWebMapper attendanceMapper;
    private final PenaltyWebMapper penaltyMapper;

    public MemberController(
            MemberService memberService,
            ContributionService contributionService,
            AttendanceRecordRepositoryPort attendanceRecords,
            PenaltyRepositoryPort penalties,
            MeetingChairpersonHistoryRepositoryPort chairHistory,
            CurrentUserPort currentUser,
            MemberWebMapper memberMapper,
            ContributionWebMapper contributionMapper,
            AttendanceWebMapper attendanceMapper,
            PenaltyWebMapper penaltyMapper
    ) {
        this.memberService = memberService;
        this.contributionService = contributionService;
        this.attendanceRecords = attendanceRecords;
        this.penalties = penalties;
        this.chairHistory = chairHistory;
        this.currentUser = currentUser;
        this.memberMapper = memberMapper;
        this.contributionMapper = contributionMapper;
        this.attendanceMapper = attendanceMapper;
        this.penaltyMapper = penaltyMapper;
    }

    @GetMapping
    @PreAuthorize("hasRole('PRESIDENT')")
    Page<MemberDtos.MemberResponse> list(Pageable pageable) {
        return memberService.list(pageable).map(memberMapper::toResponse);
    }

    @PostMapping
    @PreAuthorize("hasRole('PRESIDENT')")
    MemberDtos.MemberResponse create(@Valid @RequestBody MemberDtos.MemberRequest request) {
        return memberMapper.toResponse(memberService.create(memberMapper.toCommand(request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PRESIDENT') or authentication.token.claims['memberId'] == #id.toString()")
    MemberDtos.MemberResponse get(@PathVariable UUID id) {
        return memberMapper.toResponse(memberService.get(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PRESIDENT')")
    MemberDtos.MemberResponse update(@PathVariable UUID id, @Valid @RequestBody MemberDtos.MemberRequest request) {
        return memberMapper.toResponse(memberService.update(id, memberMapper.toCommand(request)));
    }

    @PatchMapping("/me")
    @PreAuthorize("isAuthenticated()")
    MemberDtos.MemberResponse updateMyProfile(@Valid @RequestBody MemberDtos.ProfileUpdateRequest request) {
        UUID memberId = currentUser.memberIdOrSystem();
        if (memberId == null) {
            throw new BusinessException("CURRENT_MEMBER_NOT_FOUND", "Aucun membre n'est associé à l'utilisateur connecté");
        }
        return memberMapper.toResponse(memberService.updateProfile(memberId, memberMapper.toProfileCommand(request)));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('PRESIDENT')")
    MemberDtos.MemberResponse deactivate(@PathVariable UUID id) {
        return memberMapper.toResponse(memberService.changeStatus(id, MemberStatus.INACTIVE, AuditAction.MEMBER_UPDATED, "Désactivation"));
    }

    @PatchMapping("/{id}/suspend")
    @PreAuthorize("hasRole('PRESIDENT')")
    MemberDtos.MemberResponse suspend(@PathVariable UUID id) {
        return memberMapper.toResponse(memberService.changeStatus(id, MemberStatus.SUSPENDED, AuditAction.MEMBER_SUSPENDED, "Suspension"));
    }

    @PatchMapping("/{id}/reactivate")
    @PreAuthorize("hasRole('PRESIDENT')")
    MemberDtos.MemberResponse reactivate(@PathVariable UUID id) {
        return memberMapper.toResponse(memberService.changeStatus(id, MemberStatus.ACTIVE, AuditAction.MEMBER_REACTIVATED, "Réactivation"));
    }

    @GetMapping("/{id}/contributions")
    @PreAuthorize("hasRole('PRESIDENT') or authentication.token.claims['memberId'] == #id.toString()")
    List<ContributionDtos.ContributionResponse> contributions(@PathVariable UUID id) {
        Member member = memberService.get(id);
        String memberFullName = member.getFirstName() + " " + member.getLastName();
        return contributionService.byMember(id).stream().map(contribution -> toContributionResponse(contribution, memberFullName)).toList();
    }

    @GetMapping("/{id}/attendance")
    @PreAuthorize("hasRole('PRESIDENT') or authentication.token.claims['memberId'] == #id.toString()")
    List<AttendanceDtos.AttendanceResponse> attendance(@PathVariable UUID id) {
        return attendanceRecords.findAllByMemberIdOrderByExpectedStartAtDesc(id).stream().map(attendanceMapper::toResponse).toList();
    }

    @GetMapping("/{id}/penalties")
    @PreAuthorize("hasRole('PRESIDENT') or authentication.token.claims['memberId'] == #id.toString()")
    List<PenaltyDtos.PenaltyResponse> penalties(@PathVariable UUID id) {
        return penalties.findAllByMemberIdOrderByCreatedAtDesc(id).stream().map(penaltyMapper::toResponse).toList();
    }

    @GetMapping("/{id}/chair-history")
    @PreAuthorize("hasRole('PRESIDENT') or authentication.token.claims['memberId'] == #id.toString()")
    List<ChairHistoryResponse> chairHistory(@PathVariable UUID id) {
        return chairHistory.findAllByMemberIdOrderByAssignedAtDesc(id).stream()
                .map(history -> new ChairHistoryResponse(history.getId(), history.getMeetingId(), history.getMemberId(),
                        history.getAssignmentType(), history.getAssignedBy(), history.getReason(), history.getAssignedAt(),
                        history.getCompletedAt()))
                .toList();
    }

    public record ChairHistoryResponse(UUID id, UUID meetingId, UUID memberId, String assignmentType, UUID assignedBy,
                                       String reason, Instant assignedAt, Instant completedAt) {
    }

    private ContributionDtos.ContributionResponse toContributionResponse(Contribution contribution, String memberFullName) {
        ContributionDtos.ContributionResponse response = contributionMapper.toResponse(contribution);
        return new ContributionDtos.ContributionResponse(
                response.id(),
                response.cycleId(),
                response.memberId(),
                memberFullName,
                response.expectedAmount(),
                response.paidAmount(),
                response.remainingAmount(),
                response.currency(),
                response.status(),
                response.paidAt(),
                response.validatedBy(),
                response.validatedAt(),
                response.lastPaymentMethod(),
                response.lastTransactionReference(),
                response.lastProofUrl(),
                response.lastPaymentAt()
        );
    }
}
