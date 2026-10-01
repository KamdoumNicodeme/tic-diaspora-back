package com.ticdiaspora.meeting.infrastructure.web;

import com.ticdiaspora.audit.infrastructure.web.AuditService;
import com.ticdiaspora.meeting.domain.ChairpersonCandidate;
import com.ticdiaspora.meeting.domain.ChairpersonRotationPolicy;
import com.ticdiaspora.meeting.infrastructure.persistence.MeetingChairpersonHistoryEntity;
import com.ticdiaspora.meeting.infrastructure.persistence.MeetingChairpersonHistoryJpaRepository;
import com.ticdiaspora.meeting.infrastructure.persistence.MeetingEntity;
import com.ticdiaspora.meeting.infrastructure.persistence.MeetingJpaRepository;
import com.ticdiaspora.member.infrastructure.persistence.MemberEntity;
import com.ticdiaspora.member.infrastructure.persistence.MemberJpaRepository;
import com.ticdiaspora.notification.infrastructure.web.NotificationService;
import com.ticdiaspora.shared.domain.enums.AuditAction;
import com.ticdiaspora.shared.domain.enums.MeetingStatus;
import com.ticdiaspora.shared.domain.enums.MemberStatus;
import com.ticdiaspora.shared.domain.enums.NotificationType;
import com.ticdiaspora.shared.domain.exception.BusinessException;
import com.ticdiaspora.shared.domain.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MeetingService {

    private final MeetingJpaRepository meetings;
    private final MeetingChairpersonHistoryJpaRepository chairHistory;
    private final MemberJpaRepository members;
    private final ChairpersonRotationPolicy rotationPolicy;
    private final AuditService auditService;
    private final NotificationService notificationService;

    public MeetingService(
            MeetingJpaRepository meetings,
            MeetingChairpersonHistoryJpaRepository chairHistory,
            MemberJpaRepository members,
            ChairpersonRotationPolicy rotationPolicy,
            AuditService auditService,
            NotificationService notificationService
    ) {
        this.meetings = meetings;
        this.chairHistory = chairHistory;
        this.members = members;
        this.rotationPolicy = rotationPolicy;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public Page<MeetingEntity> list(Pageable pageable) {
        return meetings.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public MeetingEntity get(UUID id) {
        return meetings.findById(id).orElseThrow(() -> new NotFoundException("Réunion", id));
    }

    @Transactional
    public MeetingEntity create(MeetingDtos.MeetingRequest request) {
        MeetingEntity meeting = new MeetingEntity();
        apply(request, meeting);
        UUID chairpersonId = request.chairpersonId() != null ? request.chairpersonId() : proposeNextChairperson();
        ensureActiveMember(chairpersonId);
        meeting.setChairpersonId(chairpersonId);
        meeting = meetings.save(meeting);
        saveChairHistory(meeting.getId(), chairpersonId, "AUTO_OR_MANUAL", "Création réunion");
        notifyChairperson(chairpersonId, meeting);
        return meeting;
    }

    @Transactional
    public List<MeetingEntity> generateYear(int year, LocalTime startTime, LocalTime endTime, String onlineLink) {
        List<MeetingEntity> generated = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            LocalDate date = LocalDate.of(year, month, 1).with(TemporalAdjusters.firstInMonth(DayOfWeek.SUNDAY));
            if (meetings.existsByMeetingDate(date)) {
                continue;
            }
            MeetingDtos.MeetingRequest request = new MeetingDtos.MeetingRequest(
                    "Réunion mensuelle TIC Diaspora - %02d/%d".formatted(month, year),
                    date,
                    startTime,
                    endTime,
                    onlineLink,
                    null,
                    null,
                    null,
                    null
            );
            generated.add(create(request));
        }
        return generated;
    }

    @Transactional
    public MeetingEntity update(UUID id, MeetingDtos.MeetingRequest request) {
        MeetingEntity meeting = get(id);
        apply(request, meeting);
        if (request.chairpersonId() != null && !request.chairpersonId().equals(meeting.getChairpersonId())) {
            changeChairperson(id, request.chairpersonId(), "Modification réunion");
        }
        return meeting;
    }

    @Transactional
    public MeetingEntity start(UUID id) {
        MeetingEntity meeting = get(id);
        if (meeting.getStatus() != MeetingStatus.PLANNED) {
            throw new BusinessException("MEETING_NOT_PLANNED", "Seule une réunion planifiée peut être démarrée");
        }
        meeting.setStatus(MeetingStatus.IN_PROGRESS);
        meeting.setStartedAt(Instant.now());
        return meeting;
    }

    @Transactional
    public MeetingEntity complete(UUID id, MeetingDtos.CompleteMeetingRequest request) {
        MeetingEntity meeting = get(id);
        if (meeting.getStatus() == MeetingStatus.COMPLETED) {
            return meeting;
        }
        if (meeting.getStatus() != MeetingStatus.IN_PROGRESS && meeting.getStatus() != MeetingStatus.PLANNED) {
            throw new BusinessException("MEETING_NOT_COMPLETABLE", "La réunion ne peut pas être clôturée dans son état actuel");
        }
        meeting.setNotes(request.notes());
        meeting.setDecisionsSummary(request.decisionsSummary());
        meeting.setProjectsSummary(request.projectsSummary());
        meeting.setStatus(MeetingStatus.COMPLETED);
        meeting.setCompletedAt(Instant.now());
        if (meeting.getChairpersonId() != null) {
            MemberEntity chairperson = ensureActiveMember(meeting.getChairpersonId());
            chairperson.setTotalMeetingsChaired(chairperson.getTotalMeetingsChaired() + 1);
            chairperson.setLastChairedAt(meeting.getCompletedAt());
        }
        auditService.record(AuditAction.CHAIRPERSON_CHANGED, "Meeting", meeting.getId(), null,
                String.valueOf(meeting.getChairpersonId()), "Clôture réunion et consolidation animation");
        return meeting;
    }

    @Transactional
    public MeetingEntity cancel(UUID id, String reason) {
        MeetingEntity meeting = get(id);
        meeting.setStatus(MeetingStatus.CANCELLED);
        meeting.setCancelledAt(Instant.now());
        auditService.record(AuditAction.MEMBER_UPDATED, "Meeting", meeting.getId(), null, "CANCELLED", reason);
        return meeting;
    }

    @Transactional
    public MeetingEntity assignAutomatic(UUID id) {
        MeetingEntity meeting = get(id);
        UUID chairpersonId = proposeNextChairperson();
        meeting.setChairpersonId(chairpersonId);
        saveChairHistory(meeting.getId(), chairpersonId, "AUTO", "Proposition automatique");
        notifyChairperson(chairpersonId, meeting);
        auditService.record(AuditAction.CHAIRPERSON_CHANGED, "Meeting", meeting.getId(), null, chairpersonId.toString(), "Assignation automatique");
        return meeting;
    }

    @Transactional
    public MeetingEntity changeChairperson(UUID id, UUID chairpersonId, String reason) {
        MeetingEntity meeting = get(id);
        ensureActiveMember(chairpersonId);
        UUID old = meeting.getChairpersonId();
        meeting.setChairpersonId(chairpersonId);
        saveChairHistory(meeting.getId(), chairpersonId, "MANUAL", reason);
        notifyChairperson(chairpersonId, meeting);
        auditService.record(AuditAction.CHAIRPERSON_CHANGED, "Meeting", meeting.getId(),
                old == null ? null : old.toString(), chairpersonId.toString(), reason);
        return meeting;
    }

    public UUID proposeNextChairperson() {
        List<ChairpersonCandidate> candidates = members.findAll().stream()
                .map(member -> new ChairpersonCandidate(
                        member.getId(),
                        member.getStatus(),
                        member.getTotalMeetingsChaired(),
                        member.getLastChairedAt(),
                        member.getJoinedAt()))
                .toList();
        return rotationPolicy.proposeNextChairperson(candidates);
    }

    private void apply(MeetingDtos.MeetingRequest request, MeetingEntity meeting) {
        meeting.setTitle(request.title());
        meeting.setMeetingDate(request.meetingDate());
        meeting.setPlannedStartTime(request.plannedStartTime());
        meeting.setPlannedEndTime(request.plannedEndTime());
        meeting.setOnlineLink(request.onlineLink());
        meeting.setNotes(request.notes());
        meeting.setDecisionsSummary(request.decisionsSummary());
        meeting.setProjectsSummary(request.projectsSummary());
    }

    private MemberEntity ensureActiveMember(UUID memberId) {
        MemberEntity member = members.findById(memberId).orElseThrow(() -> new NotFoundException("Membre", memberId));
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new BusinessException("CHAIRPERSON_NOT_ACTIVE", "Le président de séance doit être un membre actif");
        }
        return member;
    }

    private void saveChairHistory(UUID meetingId, UUID memberId, String assignmentType, String reason) {
        MeetingChairpersonHistoryEntity history = new MeetingChairpersonHistoryEntity();
        history.setMeetingId(meetingId);
        history.setMemberId(memberId);
        history.setAssignmentType(assignmentType);
        history.setReason(reason);
        chairHistory.save(history);
    }

    private void notifyChairperson(UUID memberId, MeetingEntity meeting) {
        notificationService.notifyInApp(memberId, NotificationType.CHAIRPERSON_ASSIGNED,
                "Présidence de séance",
                "Vous êtes désigné pour animer la réunion du " + meeting.getMeetingDate(),
                "{\"meetingId\":\"" + meeting.getId() + "\"}");
    }
}
