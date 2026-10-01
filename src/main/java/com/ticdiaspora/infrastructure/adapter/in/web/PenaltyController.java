package com.ticdiaspora.infrastructure.adapter.in.web;
import com.ticdiaspora.infrastructure.adapter.in.web.mapper.PenaltyWebMapper;

import com.ticdiaspora.application.usecase.*;
import com.ticdiaspora.application.port.out.*;
import com.ticdiaspora.infrastructure.adapter.in.web.dto.*;

import com.ticdiaspora.domain.model.Meeting;
import com.ticdiaspora.domain.model.Member;
import com.ticdiaspora.domain.model.Penalty;
import com.ticdiaspora.domain.model.enums.PenaltyStatus;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/penalties")
public class PenaltyController {

    private final PenaltyService penaltyService;
    private final PenaltyWebMapper penaltyMapper;
    private final PenaltyRepositoryPort penalties;
    private final MemberRepositoryPort members;
    private final MeetingRepositoryPort meetings;

    public PenaltyController(
            PenaltyService penaltyService,
            PenaltyWebMapper penaltyMapper,
            PenaltyRepositoryPort penalties,
            MemberRepositoryPort members,
            MeetingRepositoryPort meetings
    ) {
        this.penaltyService = penaltyService;
        this.penaltyMapper = penaltyMapper;
        this.penalties = penalties;
        this.members = members;
        this.meetings = meetings;
    }

    @GetMapping
    @PreAuthorize("hasRole('PRESIDENT')")
    Page<PenaltyDtos.PenaltyResponse> list(@RequestParam(required = false) PenaltyStatus status, Pageable pageable) {
        Page<Penalty> page = status == null ? penaltyService.list(pageable) : penalties.findAllByStatus(status, pageable);
        return page.map(this::toResponse);
    }

    @GetMapping("/summary")
    @PreAuthorize("hasRole('PRESIDENT')")
    PenaltyDtos.PenaltySummary summary() {
        return new PenaltyDtos.PenaltySummary(
                penalties.count(),
                penalties.countByStatus(PenaltyStatus.PENDING),
                penalties.countByStatus(PenaltyStatus.PAID),
                penalties.countByStatus(PenaltyStatus.WAIVED),
                penalties.countByStatus(PenaltyStatus.CANCELLED),
                penalties.sumAmountByStatus(PenaltyStatus.PENDING),
                penalties.sumAmountByStatus(PenaltyStatus.PAID),
                penalties.sumAmountByStatus(PenaltyStatus.WAIVED),
                penalties.sumAmountByStatus(PenaltyStatus.CANCELLED)
        );
    }

    @PostMapping
    @PreAuthorize("hasRole('PRESIDENT')")
    PenaltyDtos.PenaltyResponse create(@Valid @RequestBody PenaltyDtos.PenaltyRequest request) {
        return toResponse(penaltyService.create(request.memberId(), request.meetingId(), request.type(), request.amount(), request.reason()));
    }

    @PatchMapping("/{id}/pay")
    @PreAuthorize("hasRole('PRESIDENT')")
    PenaltyDtos.PenaltyResponse pay(@PathVariable UUID id) {
        return toResponse(penaltyService.pay(id));
    }

    @PatchMapping("/{id}/waive")
    @PreAuthorize("hasRole('PRESIDENT')")
    PenaltyDtos.PenaltyResponse waive(@PathVariable UUID id, @Valid @RequestBody PenaltyDtos.WaivePenaltyRequest request) {
        return toResponse(penaltyService.waive(id, request.reason()));
    }

    private PenaltyDtos.PenaltyResponse toResponse(Penalty penalty) {
        Member member = penalty.getMemberId() == null ? null : members.findById(penalty.getMemberId()).orElse(null);
        Meeting meeting = penalty.getMeetingId() == null ? null : meetings.findById(penalty.getMeetingId()).orElse(null);
        Member validator = penalty.getValidatedBy() == null ? null : members.findById(penalty.getValidatedBy()).orElse(null);
        return new PenaltyDtos.PenaltyResponse(
                penalty.getId(),
                penalty.getMemberId(),
                member == null ? null : member.getFirstName() + " " + member.getLastName(),
                penalty.getMeetingId(),
                meeting == null ? null : meeting.getTitle(),
                penalty.getType(),
                penalty.getAmount(), penalty.getStatus(), penalty.getReason(), penalty.getCreatedAt(),
                penalty.getPaidAt(), penalty.getValidatedBy(),
                validator == null ? null : validator.getFirstName() + " " + validator.getLastName(),
                penalty.getCancellationReason()
        );
    }
}
