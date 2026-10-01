package com.ticdiaspora.member.infrastructure.web;

import com.ticdiaspora.attendance.infrastructure.persistence.AttendanceRecordJpaRepository;
import com.ticdiaspora.attendance.infrastructure.web.AttendanceDtos;
import com.ticdiaspora.contribution.infrastructure.persistence.ContributionJpaRepository;
import com.ticdiaspora.contribution.infrastructure.web.ContributionDtos;
import com.ticdiaspora.meeting.infrastructure.persistence.MeetingChairpersonHistoryJpaRepository;
import com.ticdiaspora.member.infrastructure.persistence.MemberEntity;
import com.ticdiaspora.penalty.infrastructure.persistence.PenaltyJpaRepository;
import com.ticdiaspora.penalty.infrastructure.web.PenaltyDtos;
import com.ticdiaspora.shared.domain.enums.AuditAction;
import com.ticdiaspora.shared.domain.enums.MemberStatus;
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
    private final ContributionJpaRepository contributions;
    private final AttendanceRecordJpaRepository attendanceRecords;
    private final PenaltyJpaRepository penalties;
    private final MeetingChairpersonHistoryJpaRepository chairHistory;

    public MemberController(
            MemberService memberService,
            ContributionJpaRepository contributions,
            AttendanceRecordJpaRepository attendanceRecords,
            PenaltyJpaRepository penalties,
            MeetingChairpersonHistoryJpaRepository chairHistory
    ) {
        this.memberService = memberService;
        this.contributions = contributions;
        this.attendanceRecords = attendanceRecords;
        this.penalties = penalties;
        this.chairHistory = chairHistory;
    }

    @GetMapping
    @PreAuthorize("hasRole('PRESIDENT')")
    Page<MemberDtos.MemberResponse> list(Pageable pageable) {
        return memberService.list(pageable).map(this::toResponse);
    }

    @PostMapping
    @PreAuthorize("hasRole('PRESIDENT')")
    MemberDtos.MemberResponse create(@Valid @RequestBody MemberDtos.MemberRequest request) {
        return toResponse(memberService.create(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PRESIDENT') or authentication.token.claims['memberId'] == #id.toString()")
    MemberDtos.MemberResponse get(@PathVariable UUID id) {
        return toResponse(memberService.get(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PRESIDENT')")
    MemberDtos.MemberResponse update(@PathVariable UUID id, @Valid @RequestBody MemberDtos.MemberRequest request) {
        return toResponse(memberService.update(id, request));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('PRESIDENT')")
    MemberDtos.MemberResponse deactivate(@PathVariable UUID id) {
        return toResponse(memberService.changeStatus(id, MemberStatus.INACTIVE, AuditAction.MEMBER_UPDATED, "Désactivation"));
    }

    @PatchMapping("/{id}/suspend")
    @PreAuthorize("hasRole('PRESIDENT')")
    MemberDtos.MemberResponse suspend(@PathVariable UUID id) {
        return toResponse(memberService.changeStatus(id, MemberStatus.SUSPENDED, AuditAction.MEMBER_SUSPENDED, "Suspension"));
    }

    @PatchMapping("/{id}/reactivate")
    @PreAuthorize("hasRole('PRESIDENT')")
    MemberDtos.MemberResponse reactivate(@PathVariable UUID id) {
        return toResponse(memberService.changeStatus(id, MemberStatus.ACTIVE, AuditAction.MEMBER_REACTIVATED, "Réactivation"));
    }

    @GetMapping("/{id}/contributions")
    @PreAuthorize("hasRole('PRESIDENT') or authentication.token.claims['memberId'] == #id.toString()")
    List<ContributionDtos.ContributionResponse> contributions(@PathVariable UUID id) {
        return contributions.findAllByMemberId(id).stream().map(contribution -> {
            long remaining = Math.max(0, contribution.getExpectedAmount() - contribution.getPaidAmount());
            return new ContributionDtos.ContributionResponse(
                    contribution.getId(), contribution.getCycleId(), contribution.getMemberId(), contribution.getExpectedAmount(),
                    contribution.getPaidAmount(), remaining, contribution.getCurrency(), contribution.getStatus(),
                    contribution.getPaidAt(), contribution.getValidatedBy(), contribution.getValidatedAt()
            );
        }).toList();
    }

    @GetMapping("/{id}/attendance")
    @PreAuthorize("hasRole('PRESIDENT') or authentication.token.claims['memberId'] == #id.toString()")
    List<AttendanceDtos.AttendanceResponse> attendance(@PathVariable UUID id) {
        return attendanceRecords.findAllByMemberIdOrderByExpectedStartAtDesc(id).stream()
                .map(record -> new AttendanceDtos.AttendanceResponse(record.getId(), record.getMeetingId(), record.getMemberId(),
                        record.getStatus(), record.getExpectedStartAt(), record.getArrivalAt(), record.getDelayMinutes(),
                        record.getPenaltyId(), record.getAbsenceRequestId(), record.getRecordedBy(), record.getCreatedAt()))
                .toList();
    }

    @GetMapping("/{id}/penalties")
    @PreAuthorize("hasRole('PRESIDENT') or authentication.token.claims['memberId'] == #id.toString()")
    List<PenaltyDtos.PenaltyResponse> penalties(@PathVariable UUID id) {
        return penalties.findAllByMemberIdOrderByCreatedAtDesc(id).stream()
                .map(penalty -> new PenaltyDtos.PenaltyResponse(penalty.getId(), penalty.getMemberId(), penalty.getMeetingId(),
                        penalty.getType(), penalty.getAmount(), penalty.getStatus(), penalty.getReason(), penalty.getCreatedAt(),
                        penalty.getPaidAt(), penalty.getValidatedBy(), penalty.getCancellationReason()))
                .toList();
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

    private MemberDtos.MemberResponse toResponse(MemberEntity member) {
        return new MemberDtos.MemberResponse(
                member.getId(), member.getFirstName(), member.getLastName(), member.getEmail(),
                member.getPhone(), member.getCountry(), member.getCity(), member.getFullAddress(),
                member.getJoinedAt(), member.getStatus(), member.getRole(), member.getContributionType(),
                member.getPhotoUrl(), member.getEmergencyContactName(), member.getEmergencyContactPhone(),
                member.getTotalMeetingsChaired(), member.getLastChairedAt(),
                member.getAnnualAuthorizedAbsences(), member.getAnnualUnauthorizedAbsences(),
                member.getTotalPenaltiesAmount(), member.getUnpaidPenaltiesAmount(),
                member.getCreatedAt(), member.getUpdatedAt()
        );
    }

    public record ChairHistoryResponse(UUID id, UUID meetingId, UUID memberId, String assignmentType, UUID assignedBy,
                                       String reason, Instant assignedAt, Instant completedAt) {
    }
}
