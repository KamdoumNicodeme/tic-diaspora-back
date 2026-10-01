package com.ticdiaspora.attendance.infrastructure.web;

import com.ticdiaspora.attendance.domain.AttendanceDecision;
import com.ticdiaspora.attendance.domain.AttendanceRules;
import com.ticdiaspora.attendance.infrastructure.persistence.AttendanceRecordEntity;
import com.ticdiaspora.attendance.infrastructure.persistence.AttendanceRecordJpaRepository;
import com.ticdiaspora.auth.application.CurrentUserService;
import com.ticdiaspora.meeting.infrastructure.persistence.MeetingEntity;
import com.ticdiaspora.meeting.infrastructure.persistence.MeetingJpaRepository;
import com.ticdiaspora.member.infrastructure.persistence.MemberEntity;
import com.ticdiaspora.member.infrastructure.persistence.MemberJpaRepository;
import com.ticdiaspora.notification.infrastructure.web.NotificationService;
import com.ticdiaspora.penalty.infrastructure.persistence.PenaltyEntity;
import com.ticdiaspora.penalty.infrastructure.web.PenaltyService;
import com.ticdiaspora.shared.domain.enums.AttendanceStatus;
import com.ticdiaspora.shared.domain.enums.MemberStatus;
import com.ticdiaspora.shared.domain.enums.NotificationType;
import com.ticdiaspora.shared.domain.exception.BusinessException;
import com.ticdiaspora.shared.domain.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
public class AttendanceService {

    private final AttendanceRecordJpaRepository attendanceRecords;
    private final MeetingJpaRepository meetings;
    private final MemberJpaRepository members;
    private final AttendanceRules attendanceRules;
    private final PenaltyService penaltyService;
    private final NotificationService notificationService;
    private final CurrentUserService currentUser;

    public AttendanceService(
            AttendanceRecordJpaRepository attendanceRecords,
            MeetingJpaRepository meetings,
            MemberJpaRepository members,
            AttendanceRules attendanceRules,
            PenaltyService penaltyService,
            NotificationService notificationService,
            CurrentUserService currentUser
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
    public List<AttendanceRecordEntity> byMeeting(UUID meetingId) {
        return attendanceRecords.findAllByMeetingId(meetingId);
    }

    @Transactional
    public AttendanceRecordEntity checkIn(UUID meetingId, UUID memberId, LocalDateTime arrivalAt) {
        MeetingEntity meeting = meetings.findById(meetingId).orElseThrow(() -> new NotFoundException("Réunion", meetingId));
        MemberEntity member = ensureActiveMember(memberId);
        attendanceRecords.findByMeetingIdAndMemberId(meetingId, memberId).ifPresent(existing -> {
            throw new BusinessException("ATTENDANCE_ALREADY_RECORDED", "La présence de ce membre est déjà enregistrée pour cette réunion");
        });

        LocalDateTime expectedStartAt = LocalDateTime.of(meeting.getMeetingDate(), meeting.getPlannedStartTime());
        AttendanceDecision decision = attendanceRules.evaluateArrival(expectedStartAt, arrivalAt);
        AttendanceRecordEntity record = new AttendanceRecordEntity();
        record.setMeetingId(meetingId);
        record.setMemberId(memberId);
        record.setStatus(decision.status());
        record.setExpectedStartAt(expectedStartAt);
        record.setArrivalAt(arrivalAt);
        record.setDelayMinutes(decision.delayMinutes());
        record.setRecordedBy(currentUser.memberIdOrSystem());

        if (decision.penaltyAmount() > 0) {
            PenaltyEntity penalty = penaltyService.create(memberId, meetingId, decision.penaltyType(), decision.penaltyAmount(),
                    "Retard de " + decision.delayMinutes() + " minute(s)");
            record.setPenaltyId(penalty.getId());
        }
        incrementAnnualCounters(member, decision.status());
        warnIfThresholdReached(member);
        return attendanceRecords.save(record);
    }

    @Transactional
    public AttendanceRecordEntity recordAbsence(UUID meetingId, UUID memberId, boolean authorized, UUID absenceRequestId) {
        MeetingEntity meeting = meetings.findById(meetingId).orElseThrow(() -> new NotFoundException("Réunion", meetingId));
        MemberEntity member = ensureActiveMember(memberId);
        attendanceRecords.findByMeetingIdAndMemberId(meetingId, memberId).ifPresent(existing -> {
            throw new BusinessException("ATTENDANCE_ALREADY_RECORDED", "La présence de ce membre est déjà enregistrée pour cette réunion");
        });
        AttendanceRecordEntity record = new AttendanceRecordEntity();
        record.setMeetingId(meetingId);
        record.setMemberId(memberId);
        record.setExpectedStartAt(LocalDateTime.of(meeting.getMeetingDate(), meeting.getPlannedStartTime()));
        record.setRecordedBy(currentUser.memberIdOrSystem());
        record.setAbsenceRequestId(absenceRequestId);
        if (authorized) {
            record.setStatus(AttendanceStatus.ABSENT_AUTHORIZED);
            member.setAnnualAuthorizedAbsences(member.getAnnualAuthorizedAbsences() + 1);
        } else {
            AttendanceDecision decision = attendanceRules.unauthorizedAbsence();
            record.setStatus(decision.status());
            PenaltyEntity penalty = penaltyService.create(memberId, meetingId, decision.penaltyType(), decision.penaltyAmount(),
                    "Absence non autorisée");
            record.setPenaltyId(penalty.getId());
            incrementAnnualCounters(member, decision.status());
            warnIfThresholdReached(member);
        }
        return attendanceRecords.save(record);
    }

    private MemberEntity ensureActiveMember(UUID memberId) {
        MemberEntity member = members.findById(memberId).orElseThrow(() -> new NotFoundException("Membre", memberId));
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new BusinessException("MEMBER_NOT_ACTIVE", "Seuls les membres actifs sont concernés par les présences");
        }
        return member;
    }

    private void incrementAnnualCounters(MemberEntity member, AttendanceStatus status) {
        if (attendanceRules.contributesToUnauthorizedAbsenceThreshold(status)) {
            member.setAnnualUnauthorizedAbsences(member.getAnnualUnauthorizedAbsences() + 1);
        }
    }

    private void warnIfThresholdReached(MemberEntity member) {
        if (member.getAnnualUnauthorizedAbsences() >= 3) {
            notificationService.notifyInApp(member.getId(), NotificationType.ABSENCE_THRESHOLD_WARNING,
                    "Seuil d'absences", "Vous approchez ou dépassez le seuil de 4 absences non autorisées.",
                    "{\"unauthorizedAbsences\":" + member.getAnnualUnauthorizedAbsences() + "}");
        }
    }
}
