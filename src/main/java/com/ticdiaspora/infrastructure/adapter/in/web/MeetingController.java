package com.ticdiaspora.infrastructure.adapter.in.web;
import com.ticdiaspora.infrastructure.adapter.in.web.mapper.MeetingWebMapper;

import com.ticdiaspora.application.usecase.*;
import com.ticdiaspora.application.port.out.*;
import com.ticdiaspora.infrastructure.adapter.in.web.dto.*;

import com.ticdiaspora.domain.model.Meeting;
import com.ticdiaspora.domain.model.Member;
import com.ticdiaspora.domain.model.TontineCycle;
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
import java.util.Objects;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/meetings")
public class MeetingController {

    private final MeetingService meetingService;
    private final TontineCycleService tontineCycleService;
    private final MemberRepositoryPort members;
    private final TontineCycleRepositoryPort tontineCycles;
    private final MeetingWebMapper meetingMapper;

    public MeetingController(MeetingService meetingService, TontineCycleService tontineCycleService,
                             MemberRepositoryPort members, TontineCycleRepositoryPort tontineCycles,
                             MeetingWebMapper meetingMapper) {
        this.meetingService = meetingService;
        this.tontineCycleService = tontineCycleService;
        this.members = members;
        this.tontineCycles = tontineCycles;
        this.meetingMapper = meetingMapper;
    }

    @GetMapping
    Page<MeetingDtos.MeetingResponse> list(Pageable pageable) {
        return meetingService.list(pageable).map(this::toResponse);
    }

    @PostMapping
    @PreAuthorize("hasRole('PRESIDENT')")
    MeetingDtos.MeetingResponse create(@Valid @RequestBody MeetingDtos.MeetingRequest request) {
        Meeting meeting = meetingService.create(meetingMapper.toCommand(request));
        ensureCycleForMeeting(meeting, null);
        return toResponse(meeting);
    }

    @PostMapping("/generate")
    @PreAuthorize("hasRole('PRESIDENT')")
    List<MeetingDtos.MeetingResponse> generate(
            @RequestParam int year,
            @RequestParam(defaultValue = "15:00") LocalTime startTime,
            @RequestParam(defaultValue = "17:00") LocalTime endTime,
            @RequestParam(required = false) String onlineLink
    ) {
        meetingService.generateYear(year, startTime, endTime, onlineLink);
        return meetingService.listYear(year).stream()
                .peek(meeting -> ensureCycleForMeeting(meeting, null))
                .map(this::toResponse)
                .toList();
    }

    @PatchMapping("/complete-past")
    @PreAuthorize("hasRole('PRESIDENT')")
    List<MeetingDtos.MeetingResponse> completePastMeetings() {
        return meetingService.completePastMeetings(java.time.LocalDate.now(), LocalTime.now()).stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    MeetingDtos.MeetingResponse get(@PathVariable UUID id) {
        return toResponse(meetingService.get(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PRESIDENT')")
    MeetingDtos.MeetingResponse update(@PathVariable UUID id, @Valid @RequestBody MeetingDtos.MeetingRequest request) {
        return toResponse(meetingService.update(id, meetingMapper.toCommand(request)));
    }

    @PatchMapping("/{id}/start")
    @PreAuthorize("hasRole('PRESIDENT')")
    MeetingDtos.MeetingResponse start(@PathVariable UUID id) {
        return toResponse(meetingService.start(id));
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasRole('PRESIDENT')")
    MeetingDtos.MeetingResponse complete(@PathVariable UUID id, @RequestBody MeetingDtos.CompleteMeetingRequest request) {
        return toResponse(meetingService.complete(id, meetingMapper.toCommand(request)));
    }

    @PatchMapping("/{id}/minutes")
    @PreAuthorize("hasRole('PRESIDENT')")
    MeetingDtos.MeetingResponse saveMinutes(@PathVariable UUID id, @RequestBody MeetingDtos.CompleteMeetingRequest request) {
        return toResponse(meetingService.saveMinutes(id, meetingMapper.toCommand(request)));
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

    @PatchMapping("/{id}/change-beneficiary")
    @PreAuthorize("hasRole('PRESIDENT')")
    MeetingDtos.MeetingResponse changeBeneficiary(@PathVariable UUID id, @Valid @RequestBody MeetingDtos.ChangeBeneficiaryRequest request) {
        Meeting meeting = meetingService.get(id);
        TontineCycle cycle = ensureCycleForMeeting(meeting, request.beneficiaryId(), request.secondaryBeneficiaryId());
        if (!Objects.equals(request.beneficiaryId(), cycle.getBeneficiaryId())
                || !Objects.equals(request.secondaryBeneficiaryId(), cycle.getSecondaryBeneficiaryId())) {
            tontineCycleService.changeBeneficiary(cycle.getId(), request.beneficiaryId(), request.secondaryBeneficiaryId(), request.unanimousAgreement(), request.reason());
        }
        return toResponse(meeting);
    }

    @GetMapping("/next-chairperson")
    @PreAuthorize("hasRole('PRESIDENT')")
    UUID nextChairperson() {
        return meetingService.proposeNextChairperson();
    }

    private MeetingDtos.MeetingResponse toResponse(@NotNull Meeting meeting) {
        Member chairperson = meeting.getChairpersonId() == null ? null : members.findById(meeting.getChairpersonId()).orElse(null);
        TontineCycle cycle = tontineCycles.findByMonthAndYear(meeting.getMeetingDate().getMonthValue(), meeting.getMeetingDate().getYear()).orElse(null);
        Member beneficiary = cycle == null || cycle.getBeneficiaryId() == null ? null : members.findById(cycle.getBeneficiaryId()).orElse(null);
        Member secondaryBeneficiary = cycle == null || cycle.getSecondaryBeneficiaryId() == null ? null : members.findById(cycle.getSecondaryBeneficiaryId()).orElse(null);
        return new MeetingDtos.MeetingResponse(
                meeting.getId(), meeting.getTitle(), meeting.getMeetingDate(), meeting.getPlannedStartTime(),
                meeting.getPlannedEndTime(), meeting.getOnlineLink(), meeting.getStatus(), meeting.getChairpersonId(),
                chairperson == null ? null : chairperson.getFirstName() + " " + chairperson.getLastName(),
                cycle == null ? null : cycle.getBeneficiaryId(),
                beneficiary == null ? null : beneficiary.getFirstName() + " " + beneficiary.getLastName(),
                beneficiary == null ? null : beneficiary.getOrangeMoneyNumber(),
                beneficiary == null ? null : beneficiary.getOrangeMoneyAccountName(),
                beneficiary == null ? null : beneficiary.getMtnMoneyNumber(),
                beneficiary == null ? null : beneficiary.getMtnMoneyAccountName(),
                cycle == null ? 0 : primaryExpectedAmount(cycle),
                cycle == null ? null : cycle.getSecondaryBeneficiaryId(),
                secondaryBeneficiary == null ? null : secondaryBeneficiary.getFirstName() + " " + secondaryBeneficiary.getLastName(),
                secondaryBeneficiary == null ? null : secondaryBeneficiary.getOrangeMoneyNumber(),
                secondaryBeneficiary == null ? null : secondaryBeneficiary.getOrangeMoneyAccountName(),
                secondaryBeneficiary == null ? null : secondaryBeneficiary.getMtnMoneyNumber(),
                secondaryBeneficiary == null ? null : secondaryBeneficiary.getMtnMoneyAccountName(),
                cycle == null ? 0 : secondaryExpectedAmount(cycle),
                meeting.getNotes(), meeting.getDecisionsSummary(), meeting.getProjectsSummary(),
                meeting.getCreatedAt(), meeting.getStartedAt(), meeting.getCompletedAt(), meeting.getCancelledAt()
        );
    }

    private TontineCycle ensureCycleForMeeting(Meeting meeting, UUID beneficiaryId) {
        return ensureCycleForMeeting(meeting, beneficiaryId, null);
    }

    private TontineCycle ensureCycleForMeeting(Meeting meeting, UUID beneficiaryId, UUID secondaryBeneficiaryId) {
        return tontineCycles.findByMonthAndYear(meeting.getMeetingDate().getMonthValue(), meeting.getMeetingDate().getYear())
                .orElseGet(() -> tontineCycleService.create(new com.ticdiaspora.application.port.in.TontineCycleCommand(
                        meeting.getMeetingDate().getMonthValue(),
                        meeting.getMeetingDate().getYear(),
                        beneficiaryId,
                        secondaryBeneficiaryId,
                        "Cycle et bénéficiaire(s) affectés automatiquement après génération de la réunion " + meeting.getTitle()
                )));
    }

    private long primaryExpectedAmount(TontineCycle cycle) {
        return cycle.getSecondaryBeneficiaryId() == null ? cycle.getExpectedAmount() : cycle.getExpectedAmount() / 2;
    }

    private long secondaryExpectedAmount(TontineCycle cycle) {
        return cycle.getSecondaryBeneficiaryId() == null ? 0 : cycle.getExpectedAmount() / 2;
    }
}
