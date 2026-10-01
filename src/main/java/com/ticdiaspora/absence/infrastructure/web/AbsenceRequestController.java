package com.ticdiaspora.absence.infrastructure.web;

import com.ticdiaspora.absence.infrastructure.persistence.AbsenceRequestEntity;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/absence-requests")
public class AbsenceRequestController {

    private final AbsenceRequestService absenceRequestService;

    public AbsenceRequestController(AbsenceRequestService absenceRequestService) {
        this.absenceRequestService = absenceRequestService;
    }

    @GetMapping
    @PreAuthorize("hasRole('PRESIDENT')")
    Page<AbsenceRequestDtos.AbsenceRequestResponse> list(Pageable pageable) {
        return absenceRequestService.list(pageable).map(this::toResponse);
    }

    @PostMapping
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

    private AbsenceRequestDtos.AbsenceRequestResponse toResponse(AbsenceRequestEntity request) {
        return new AbsenceRequestDtos.AbsenceRequestResponse(
                request.getId(), request.getMemberId(), request.getMeetingId(), request.getReason(), request.getRequestedAt(),
                request.getHoursBeforeMeeting(), request.getStatus(), request.getValidationComment(), request.getValidatedBy(),
                request.getValidatedAt(), request.isExceptionalApproval()
        );
    }
}
