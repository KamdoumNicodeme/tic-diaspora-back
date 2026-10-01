package com.ticdiaspora.meeting.infrastructure.web;

import com.ticdiaspora.meeting.infrastructure.persistence.MeetingEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/meetings")
public class MeetingController {

    private final MeetingService meetingService;

    public MeetingController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    @GetMapping
    Page<MeetingDtos.MeetingResponse> list(Pageable pageable) {
        return meetingService.list(pageable).map(this::toResponse);
    }

    @PostMapping
    @PreAuthorize("hasRole('PRESIDENT')")
    MeetingDtos.MeetingResponse create(@Valid @RequestBody MeetingDtos.MeetingRequest request) {
        return toResponse(meetingService.create(request));
    }

    @PostMapping("/generate")
    @PreAuthorize("hasRole('PRESIDENT')")
    List<MeetingDtos.MeetingResponse> generate(
            @RequestParam int year,
            @RequestParam(defaultValue = "15:00") LocalTime startTime,
            @RequestParam(defaultValue = "17:00") LocalTime endTime,
            @RequestParam(required = false) String onlineLink
    ) {
        return meetingService.generateYear(year, startTime, endTime, onlineLink).stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    MeetingDtos.MeetingResponse get(@PathVariable UUID id) {
        return toResponse(meetingService.get(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PRESIDENT')")
    MeetingDtos.MeetingResponse update(@PathVariable UUID id, @Valid @RequestBody MeetingDtos.MeetingRequest request) {
        return toResponse(meetingService.update(id, request));
    }

    @PatchMapping("/{id}/start")
    @PreAuthorize("hasRole('PRESIDENT')")
    MeetingDtos.MeetingResponse start(@PathVariable UUID id) {
        return toResponse(meetingService.start(id));
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasRole('PRESIDENT')")
    MeetingDtos.MeetingResponse complete(@PathVariable UUID id, @RequestBody MeetingDtos.CompleteMeetingRequest request) {
        return toResponse(meetingService.complete(id, request));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('PRESIDENT')")
    MeetingDtos.MeetingResponse cancel(@PathVariable UUID id, @RequestParam(required = false) String reason) {
        return toResponse(meetingService.cancel(id, reason));
    }

    @PatchMapping("/{id}/assign-chairperson")
    @PreAuthorize("hasRole('PRESIDENT')")
    MeetingDtos.MeetingResponse assignChairperson(@PathVariable UUID id) {
        return toResponse(meetingService.assignAutomatic(id));
    }

    @PatchMapping("/{id}/change-chairperson")
    @PreAuthorize("hasRole('PRESIDENT')")
    MeetingDtos.MeetingResponse changeChairperson(@PathVariable UUID id, @Valid @RequestBody MeetingDtos.ChangeChairpersonRequest request) {
        return toResponse(meetingService.changeChairperson(id, request.chairpersonId(), request.reason()));
    }

    @GetMapping("/next-chairperson")
    @PreAuthorize("hasRole('PRESIDENT')")
    UUID nextChairperson() {
        return meetingService.proposeNextChairperson();
    }

    private MeetingDtos.MeetingResponse toResponse(@NotNull MeetingEntity meeting) {
        return new MeetingDtos.MeetingResponse(
                meeting.getId(), meeting.getTitle(), meeting.getMeetingDate(), meeting.getPlannedStartTime(),
                meeting.getPlannedEndTime(), meeting.getOnlineLink(), meeting.getStatus(), meeting.getChairpersonId(),
                meeting.getNotes(), meeting.getDecisionsSummary(), meeting.getProjectsSummary(),
                meeting.getCreatedAt(), meeting.getStartedAt(), meeting.getCompletedAt(), meeting.getCancelledAt()
        );
    }
}
