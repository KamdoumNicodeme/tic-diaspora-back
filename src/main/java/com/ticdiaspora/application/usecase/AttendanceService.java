package com.ticdiaspora.application.usecase;
import com.ticdiaspora.application.port.in.*;
import com.ticdiaspora.application.port.out.*;

import com.ticdiaspora.domain.model.AttendanceDecision;
import com.ticdiaspora.domain.service.AttendanceRules;
import com.ticdiaspora.domain.model.AttendanceRecord;
import com.ticdiaspora.application.port.out.AttendanceRecordRepositoryPort;
import com.ticdiaspora.application.port.out.CurrentUserPort;
import com.ticdiaspora.domain.model.Meeting;
import com.ticdiaspora.application.port.out.MeetingRepositoryPort;
import com.ticdiaspora.domain.model.Member;
import com.ticdiaspora.application.port.out.MemberRepositoryPort;
import com.ticdiaspora.domain.model.Penalty;
import com.ticdiaspora.domain.model.enums.AttendanceStatus;
import com.ticdiaspora.domain.model.enums.MemberStatus;
import com.ticdiaspora.domain.model.enums.NotificationType;
import com.ticdiaspora.domain.exception.BusinessException;
import com.ticdiaspora.domain.exception.NotFoundException;
import com.ticdiaspora.application.annotation.UseCase;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@UseCase
public class AttendanceService {

    private final AttendanceRecordRepositoryPort attendanceRecords;
    private final MeetingRepositoryPort meetings;
    private final MemberRepositoryPort members;
    private final AttendanceRules attendanceRules;
    private final PenaltyService penaltyService;
    private final NotificationService notificationService;
    private final CurrentUserPort currentUser;

    public AttendanceService(
            AttendanceRecordRepositoryPort attendanceRecords,
            MeetingRepositoryPort meetings,
            MemberRepositoryPort members,
            AttendanceRules attendanceRules,
            PenaltyService penaltyService,
            NotificationService notificationService,
            CurrentUserPort currentUser
    ) {
        this.attendanceRecords = attendanceRecords;
        this.meetings = meetings;
        this.members = members;
        this.attendanceRules = attendanceRules;
        this.penaltyService = penaltyService;
        this.notificationService = notificationService;
        this.currentUser = currentUser;
    }

    @Transactional(readOnly = true)
    public List<AttendanceRecord> byMeeting(UUID meetingId) {
        return attendanceRecords.findAllByMeetingId(meetingId);
    }

    @Transactional
    public AttendanceRecord checkIn(UUID meetingId, UUID memberId, LocalDateTime arrivalAt) {
        Meeting meeting = meetings.findById(meetingId).orElseThrow(() -> new NotFoundException("Réunion", meetingId));
        Member member = ensureActiveMember(memberId);
        attendanceRecords.findByMeetingIdAndMemberId(meetingId, memberId).ifPresent(existing -> {
            throw new BusinessException("ATTENDANCE_ALREADY_RECORDED", "La présence de ce membre est déjà enregistrée pour cette réunion");
        });

        LocalDateTime expectedStartAt = LocalDateTime.of(meeting.getMeetingDate(), meeting.getPlannedStartTime());
        AttendanceDecision decision = attendanceRules.evaluateArrival(expectedStartAt, arrivalAt);
        AttendanceRecord record = new AttendanceRecord();
        record.setMeetingId(meetingId);
        record.setMemberId(memberId);
        record.setStatus(decision.status());
        record.setExpectedStartAt(expectedStartAt);
        record.setArrivalAt(arrivalAt);
        record.setDelayMinutes(decision.delayMinutes());
        record.setRecordedBy(currentUser.memberIdOrSystem());

        if (decision.penaltyAmount() > 0) {
            Penalty penalty = penaltyService.create(memberId, meetingId, decision.penaltyType(), decision.penaltyAmount(),
                    "Retard de " + decision.delayMinutes() + " minute(s)");
            record.setPenaltyId(penalty.getId());
        }
        incrementAnnualCounters(member, decision.status());
        member = members.save(member);
        warnIfThresholdReached(member);
        return attendanceRecords.save(record);
    }

    @Transactional
    public AttendanceRecord recordAbsence(UUID meetingId, UUID memberId, boolean authorized, UUID absenceRequestId) {
        Meeting meeting = meetings.findById(meetingId).orElseThrow(() -> new NotFoundException("Réunion", meetingId));
        Member member = ensureActiveMember(memberId);
        attendanceRecords.findByMeetingIdAndMemberId(meetingId, memberId).ifPresent(existing -> {
            throw new BusinessException("ATTENDANCE_ALREADY_RECORDED", "La présence de ce membre est déjà enregistrée pour cette réunion");
        });
        AttendanceRecord record = new AttendanceRecord();
        record.setMeetingId(meetingId);
        record.setMemberId(memberId);
        record.setExpectedStartAt(LocalDateTime.of(meeting.getMeetingDate(), meeting.getPlannedStartTime()));
        record.setRecordedBy(currentUser.memberIdOrSystem());
        record.setAbsenceRequestId(absenceRequestId);
        if (authorized) {
            record.setStatus(AttendanceStatus.ABSENT_AUTHORIZED);
            member.setAnnualAuthorizedAbsences(member.getAnnualAuthorizedAbsences() + 1);
            member = members.save(member);
        } else {
            AttendanceDecision decision = attendanceRules.unauthorizedAbsence();
            record.setStatus(decision.status());
            Penalty penalty = penaltyService.create(memberId, meetingId, decision.penaltyType(), decision.penaltyAmount(),
                    "Absence non autorisée");
            record.setPenaltyId(penalty.getId());
            incrementAnnualCounters(member, decision.status());
            member = members.save(member);
            warnIfThresholdReached(member);
        }
        return attendanceRecords.save(record);
    }

    private Member ensureActiveMember(UUID memberId) {
        Member member = members.findById(memberId).orElseThrow(() -> new NotFoundException("Membre", memberId));
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new BusinessException("MEMBER_NOT_ACTIVE", "Seuls les membres actifs sont concernés par les présences");
        }
        return member;
    }

    private void incrementAnnualCounters(Member member, AttendanceStatus status) {
        if (attendanceRules.contributesToUnauthorizedAbsenceThreshold(status)) {
            member.setAnnualUnauthorizedAbsences(member.getAnnualUnauthorizedAbsences() + 1);
        }
    }

    private void warnIfThresholdReached(Member member) {
        if (member.getAnnualUnauthorizedAbsences() >= 3) {
            notificationService.notifyInApp(member.getId(), NotificationType.ABSENCE_THRESHOLD_WARNING,
                    "Seuil d'absences", "Vous approchez ou dépassez le seuil de 4 absences non autorisées.",
                    "{\"unauthorizedAbsences\":" + member.getAnnualUnauthorizedAbsences() + "}");
        }
    }
}
