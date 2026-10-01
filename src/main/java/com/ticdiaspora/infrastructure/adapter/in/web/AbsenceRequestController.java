package com.ticdiaspora.infrastructure.adapter.in.web;

import com.ticdiaspora.application.port.out.MeetingRepositoryPort;
import com.ticdiaspora.application.port.out.MemberRepositoryPort;
import com.ticdiaspora.application.usecase.*;
import com.ticdiaspora.domain.model.AbsenceRequest;
import com.ticdiaspora.domain.model.Meeting;
import com.ticdiaspora.domain.model.Member;
import com.ticdiaspora.infrastructure.adapter.in.web.dto.*;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/absence-requests")
public class AbsenceRequestController {

    private final AbsenceRequestService absenceRequestService;
    private final MemberRepositoryPort members;
    private final MeetingRepositoryPort meetings;

    public AbsenceRequestController(
            AbsenceRequestService absenceRequestService,
            MemberRepositoryPort members,
            MeetingRepositoryPort meetings
    ) {
        this.absenceRequestService = absenceRequestService;
        this.members = members;
        this.meetings = meetings;
    }

    @GetMapping
    @PreAuthorize("hasRole('PRESIDENT')")
    Page<AbsenceRequestDtos.AbsenceRequestResponse> list(Pageable pageable) {
        return absenceRequestService.list(pageable).map(this::toResponse);
    }

    @GetMapping("/mine")
    List<AbsenceRequestDtos.AbsenceRequestResponse> mine() {
        return absenceRequestService.mine().stream().map(this::toResponse).toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('PRESIDENT') or authentication.token.claims['memberId'] == #request.memberId().toString()")
    AbsenceRequestDtos.AbsenceRequestResponse create(@Valid @RequestBody AbsenceRequestDtos.CreateAbsenceRequest request) {
        return toResponse(absenceRequestService.create(request.memberId(), request.meetingId(), request.reason()));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('PRESIDENT')")
    AbsenceRequestDtos.AbsenceRequestResponse approve(@PathVariable UUID id, @RequestBody AbsenceRequestDtos.ValidateAbsenceRequest request) {
        return toResponse(absenceRequestService.approve(id, request.validationComment(), request.exceptionalApproval()));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('PRESIDENT')")
    AbsenceRequestDtos.AbsenceRequestResponse reject(@PathVariable UUID id, @RequestBody AbsenceRequestDtos.ValidateAbsenceRequest request) {
        return toResponse(absenceRequestService.reject(id, request.validationComment()));
    }

    private AbsenceRequestDtos.AbsenceRequestResponse toResponse(AbsenceRequest request) {
        Member member = members.findById(request.getMemberId()).orElse(null);
        Meeting meeting = meetings.findById(request.getMeetingId()).orElse(null);
        return new AbsenceRequestDtos.AbsenceRequestResponse(
                request.getId(),
                request.getMemberId(),
                member == null ? request.getMemberId().toString() : member.getFirstName() + " " + member.getLastName(),
                request.getMeetingId(),
                meeting == null ? request.getMeetingId().toString() : meeting.getTitle(),
                meeting == null ? null : meeting.getMeetingDate(),
                meeting == null ? null : meeting.getPlannedStartTime(),
                request.getReason(),
                request.getRequestedAt(),
                request.getHoursBeforeMeeting(),
                request.getStatus(),
                request.getValidationComment(),
                request.getValidatedBy(),
                request.getValidatedAt(),
                request.isExceptionalApproval()
        );
    }
}
