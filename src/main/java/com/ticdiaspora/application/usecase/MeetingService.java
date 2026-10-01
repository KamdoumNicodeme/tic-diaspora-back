package com.ticdiaspora.application.usecase;
import com.ticdiaspora.application.port.in.*;
import com.ticdiaspora.application.port.out.*;

import com.ticdiaspora.domain.model.ChairpersonCandidate;
import com.ticdiaspora.domain.service.ChairpersonRotationPolicy;
import com.ticdiaspora.domain.model.MeetingChairpersonHistory;
import com.ticdiaspora.application.port.out.MeetingChairpersonHistoryRepositoryPort;
import com.ticdiaspora.domain.model.Meeting;
import com.ticdiaspora.application.port.out.MeetingRepositoryPort;
import com.ticdiaspora.domain.model.Member;
import com.ticdiaspora.application.port.out.MemberRepositoryPort;
import com.ticdiaspora.domain.model.enums.AuditAction;
import com.ticdiaspora.domain.model.enums.MeetingStatus;
import com.ticdiaspora.domain.model.enums.MemberStatus;
import com.ticdiaspora.domain.model.enums.NotificationType;
import com.ticdiaspora.domain.exception.BusinessException;
import com.ticdiaspora.domain.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.ticdiaspora.application.annotation.UseCase;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@UseCase
public class MeetingService {

    private final MeetingRepositoryPort meetings;
    private final MeetingChairpersonHistoryRepositoryPort chairHistory;
    private final MemberRepositoryPort members;
    private final ChairpersonRotationPolicy rotationPolicy;
    private final AuditService auditService;
    private final NotificationService notificationService;

    public MeetingService(
            MeetingRepositoryPort meetings,
            MeetingChairpersonHistoryRepositoryPort chairHistory,
            MemberRepositoryPort members,
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
    public Page<Meeting> list(Pageable pageable) {
        return meetings.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Meeting get(UUID id) {
        return meetings.findById(id).orElseThrow(() -> new NotFoundException("Réunion", id));
    }

    @Transactional(readOnly = true)
    public List<Meeting> listYear(int year) {
        return meetings.findAllByMeetingDateBetweenOrderByMeetingDateAsc(
                LocalDate.of(year, 1, 1),
                LocalDate.of(year, 12, 31)
        );
    }

    @Transactional
    public Meeting create(MeetingCommand request) {
        Meeting meeting = new Meeting();
        apply(request, meeting);
        meeting.setStatus(MeetingStatus.PLANNED);
        UUID chairpersonId = request.chairpersonId() != null ? request.chairpersonId() : proposeNextChairperson();
        ensureActiveMember(chairpersonId);
        meeting.setChairpersonId(chairpersonId);
        meeting = meetings.save(meeting);
        saveChairHistory(meeting.getId(), chairpersonId, "AUTO_OR_MANUAL", "Création réunion");
        notifyChairperson(chairpersonId, meeting);
        return meeting;
    }

    @Transactional
    public List<Meeting> generateYear(int year, LocalTime startTime, LocalTime endTime, String onlineLink) {
        List<Meeting> generated = new ArrayList<>();
        List<Member> rotationMembers = members.findAllByStatus(MemberStatus.ACTIVE).stream()
                .sorted(Comparator
                        .comparingInt(Member::getTotalMeetingsChaired)
                        .thenComparing(member -> member.getLastChairedAt() == null ? Instant.EPOCH : member.getLastChairedAt())
                        .thenComparing(Member::getJoinedAt)
                        .thenComparing(Member::getId))
                .toList();
        if (rotationMembers.isEmpty()) {
            throw new BusinessException("NO_ELIGIBLE_CHAIRPERSON", "Aucun membre actif éligible pour animer la réunion");
        }
        int rotationIndex = 0;
        for (int month = 1; month <= 12; month++) {
            LocalDate date = LocalDate.of(year, month, 1).with(TemporalAdjusters.firstInMonth(DayOfWeek.SUNDAY));
            if (meetings.existsByMeetingDate(date)) {
                continue;
            }
            UUID chairpersonId = rotationMembers.get(rotationIndex % rotationMembers.size()).getId();
            rotationIndex++;
            MeetingCommand request = new MeetingCommand(
                    "Réunion mensuelle TIC Diaspora - %02d/%d".formatted(month, year),
                    date,
                    startTime,
                    endTime,
                    onlineLink,
                    chairpersonId,
                    null,
                    null,
                    null
            );
            generated.add(create(request));
        }
        return generated;
    }

    @Transactional
    public Meeting update(UUID id, MeetingCommand request) {
        Meeting meeting = get(id);
        apply(request, meeting);
        if (request.chairpersonId() != null && !request.chairpersonId().equals(meeting.getChairpersonId())) {
            ensureActiveMember(request.chairpersonId());
            UUID old = meeting.getChairpersonId();
            meeting.setChairpersonId(request.chairpersonId());
            saveChairHistory(id, request.chairpersonId(), "MANUAL", "Modification réunion");
            notifyChairperson(request.chairpersonId(), meeting);
            auditService.record(AuditAction.CHAIRPERSON_CHANGED, "Meeting", id,
                    old == null ? null : old.toString(), request.chairpersonId().toString(), "Modification réunion");
        }
        return meetings.save(meeting);
    }

    @Transactional
    public Meeting start(UUID id) {
        Meeting meeting = get(id);
        if (meeting.getStatus() != MeetingStatus.PLANNED) {
            throw new BusinessException("MEETING_NOT_PLANNED", "Seule une réunion planifiée peut être démarrée");
        }
        meeting.setStatus(MeetingStatus.IN_PROGRESS);
        meeting.setStartedAt(Instant.now());
        return meetings.save(meeting);
    }

    @Transactional
    public Meeting complete(UUID id, CompleteMeetingCommand request) {
        Meeting meeting = get(id);
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
            Member chairperson = ensureActiveMember(meeting.getChairpersonId());
            chairperson.setTotalMeetingsChaired(chairperson.getTotalMeetingsChaired() + 1);
            chairperson.setLastChairedAt(meeting.getCompletedAt());
            members.save(chairperson);
        }
        meeting = meetings.save(meeting);
        auditService.record(AuditAction.CHAIRPERSON_CHANGED, "Meeting", meeting.getId(), null,
                String.valueOf(meeting.getChairpersonId()), "Clôture réunion et consolidation animation");
        return meeting;
    }

    @Transactional
    public Meeting saveMinutes(UUID id, CompleteMeetingCommand request) {
        Meeting meeting = get(id);
        String oldValue = "notes=%s; decisions=%s; projects=%s".formatted(
                meeting.getNotes(),
                meeting.getDecisionsSummary(),
                meeting.getProjectsSummary()
        );
        meeting.setNotes(request.notes());
        meeting.setDecisionsSummary(request.decisionsSummary());
        meeting.setProjectsSummary(request.projectsSummary());
        meeting = meetings.save(meeting);
        String newValue = "notes=%s; decisions=%s; projects=%s".formatted(
                meeting.getNotes(),
                meeting.getDecisionsSummary(),
                meeting.getProjectsSummary()
        );
        auditService.record(AuditAction.MEETING_MINUTES_UPDATED, "Meeting", id, oldValue, newValue, "Sauvegarde compte rendu réunion");
        return meeting;
    }

    @Transactional
    public List<Meeting> completePastMeetings(LocalDate today, LocalTime now) {
        return meetings.findAll().stream()
                .filter(meeting -> meeting.getStatus() == MeetingStatus.PLANNED || meeting.getStatus() == MeetingStatus.IN_PROGRESS)
                .filter(meeting -> isPastMeeting(meeting, today, now))
                .map(meeting -> {
                    meeting.setStatus(MeetingStatus.COMPLETED);
                    meeting.setCompletedAt(Instant.now());
                    Meeting saved = meetings.save(meeting);
                    auditService.record(AuditAction.MEETING_AUTO_COMPLETED, "Meeting", saved.getId(), null,
                            MeetingStatus.COMPLETED.name(), "Clôture automatique des réunions passées");
                    return saved;
                })
                .toList();
    }

    @Transactional
    public Meeting cancel(UUID id, String reason) {
        Meeting meeting = get(id);
        meeting.setStatus(MeetingStatus.CANCELLED);
        meeting.setCancelledAt(Instant.now());
        meeting = meetings.save(meeting);
        auditService.record(AuditAction.MEMBER_UPDATED, "Meeting", meeting.getId(), null, "CANCELLED", reason);
        return meeting;
    }

    @Transactional
    public Meeting assignAutomatic(UUID id) {
        Meeting meeting = get(id);
        UUID chairpersonId = proposeNextChairperson();
        meeting.setChairpersonId(chairpersonId);
        meeting = meetings.save(meeting);
        saveChairHistory(meeting.getId(), chairpersonId, "AUTO", "Proposition automatique");
        notifyChairperson(chairpersonId, meeting);
        auditService.record(AuditAction.CHAIRPERSON_CHANGED, "Meeting", meeting.getId(), null, chairpersonId.toString(), "Assignation automatique");
        return meeting;
    }

    @Transactional
    public Meeting changeChairperson(UUID id, UUID chairpersonId, String reason) {
        Meeting meeting = get(id);
        ensureActiveMember(chairpersonId);
        UUID old = meeting.getChairpersonId();
        meeting.setChairpersonId(chairpersonId);
        meeting = meetings.save(meeting);
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

    private void apply(MeetingCommand request, Meeting meeting) {
        meeting.setTitle(request.title());
        meeting.setMeetingDate(request.meetingDate());
        meeting.setPlannedStartTime(request.plannedStartTime());
        meeting.setPlannedEndTime(request.plannedEndTime());
        meeting.setOnlineLink(request.onlineLink());
        meeting.setNotes(request.notes());
        meeting.setDecisionsSummary(request.decisionsSummary());
        meeting.setProjectsSummary(request.projectsSummary());
    }

    private boolean isPastMeeting(Meeting meeting, LocalDate today, LocalTime now) {
        if (meeting.getMeetingDate().isBefore(today)) {
            return true;
        }
        return meeting.getMeetingDate().isEqual(today)
                && meeting.getPlannedEndTime() != null
                && meeting.getPlannedEndTime().isBefore(now);
    }

    private Member ensureActiveMember(UUID memberId) {
        Member member = members.findById(memberId).orElseThrow(() -> new NotFoundException("Membre", memberId));
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new BusinessException("CHAIRPERSON_NOT_ACTIVE", "Le président de séance doit être un membre actif");
        }
        return member;
    }

    private void saveChairHistory(UUID meetingId, UUID memberId, String assignmentType, String reason) {
        MeetingChairpersonHistory history = new MeetingChairpersonHistory();
        history.setMeetingId(meetingId);
        history.setMemberId(memberId);
        history.setAssignmentType(assignmentType);
        history.setReason(reason);
        history.setAssignedAt(Instant.now());
        chairHistory.save(history);
    }

    private void notifyChairperson(UUID memberId, Meeting meeting) {
        notificationService.notifyInApp(memberId, NotificationType.CHAIRPERSON_ASSIGNED,
                "Présidence de séance",
                "Vous êtes désigné pour animer la réunion du " + meeting.getMeetingDate(),
                "{\"meetingId\":\"" + meeting.getId() + "\"}");
    }
}
