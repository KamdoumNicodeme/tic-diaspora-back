package com.ticdiaspora.absence.infrastructure.web;

import com.ticdiaspora.absence.domain.AbsenceClassification;
import com.ticdiaspora.absence.domain.AbsenceRules;
import com.ticdiaspora.absence.infrastructure.persistence.AbsenceRequestEntity;
import com.ticdiaspora.absence.infrastructure.persistence.AbsenceRequestJpaRepository;
import com.ticdiaspora.audit.infrastructure.web.AuditService;
import com.ticdiaspora.auth.application.CurrentUserService;
import com.ticdiaspora.meeting.infrastructure.persistence.MeetingEntity;
import com.ticdiaspora.meeting.infrastructure.persistence.MeetingJpaRepository;
import com.ticdiaspora.member.infrastructure.persistence.MemberJpaRepository;
import com.ticdiaspora.notification.infrastructure.web.NotificationService;
import com.ticdiaspora.shared.domain.enums.AbsenceRequestStatus;
import com.ticdiaspora.shared.domain.enums.AuditAction;
import com.ticdiaspora.shared.domain.enums.NotificationType;
import com.ticdiaspora.shared.domain.exception.BusinessException;
import com.ticdiaspora.shared.domain.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

@Service
public class AbsenceRequestService {

    private final AbsenceRequestJpaRepository absenceRequests;
    private final MeetingJpaRepository meetings;
    private final MemberJpaRepository members;
    private final AbsenceRules absenceRules;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final CurrentUserService currentUser;

    public AbsenceRequestService(
            AbsenceRequestJpaRepository absenceRequests,
            MeetingJpaRepository meetings,
            MemberJpaRepository members,
            AbsenceRules absenceRules,
            NotificationService notificationService,
            AuditService auditService,
            CurrentUserService currentUser
    ) {
        this.absenceRequests = absenceRequests;
        this.meetings = meetings;
        this.members = members;
        this.absenceRules = absenceRules;
        this.notificationService = notificationService;
        this.auditService = auditService;
        this.currentUser = currentUser;
    }

    @Transactional(readOnly = true)
    public Page<AbsenceRequestEntity> list(Pageable pageable) {
        return absenceRequests.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public AbsenceRequestEntity get(UUID id) {
        return absenceRequests.findById(id).orElseThrow(() -> new NotFoundException("Demande d'absence", id));
    }

    @Transactional
    public AbsenceRequestEntity create(UUID memberId, UUID meetingId, String reason) {
        members.findById(memberId).orElseThrow(() -> new NotFoundException("Membre", memberId));
        MeetingEntity meeting = meetings.findById(meetingId).orElseThrow(() -> new NotFoundException("Réunion", meetingId));
        Instant requestedAt = Instant.now();
        Instant meetingStartAt = meeting.getMeetingDate()
                .atTime(meeting.getPlannedStartTime())
                .atZone(ZoneId.systemDefault())
                .toInstant();
        AbsenceClassification classification = absenceRules.classify(requestedAt, meetingStartAt);
        AbsenceRequestEntity absenceRequest = new AbsenceRequestEntity();
        absenceRequest.setMemberId(memberId);
        absenceRequest.setMeetingId(meetingId);
        absenceRequest.setReason(reason);
        absenceRequest.setRequestedAt(requestedAt);
        absenceRequest.setHoursBeforeMeeting(classification.hoursBeforeMeeting());
        absenceRequest.setStatus(classification.status());
        if (classification.status() == AbsenceRequestStatus.AUTO_APPROVED) {
            absenceRequest.setValidatedAt(requestedAt);
        }
        return absenceRequests.save(absenceRequest);
    }

    @Transactional
    public AbsenceRequestEntity approve(UUID id, String validationComment, boolean exceptionalApproval) {
        AbsenceRequestEntity request = get(id);
        if (request.getStatus() == AbsenceRequestStatus.LATE_REQUEST
                && !absenceRules.canApproveLateRequest(exceptionalApproval, validationComment)) {
            throw new BusinessException("LATE_ABSENCE_REQUIRES_EXCEPTION", "Une demande tardive exige une validation exceptionnelle avec commentaire");
        }
        request.setStatus(AbsenceRequestStatus.APPROVED);
        request.setValidationComment(validationComment);
        request.setExceptionalApproval(exceptionalApproval);
        request.setValidatedBy(currentUser.memberIdOrSystem());
        request.setValidatedAt(Instant.now());
        notificationService.notifyInApp(request.getMemberId(), NotificationType.ABSENCE_APPROVED,
                "Absence approuvée", "Votre demande d'absence a été approuvée.", "{\"absenceRequestId\":\"" + request.getId() + "\"}");
        if (exceptionalApproval) {
            auditService.record(AuditAction.ABSENCE_EXCEPTIONALLY_APPROVED, "AbsenceRequest", request.getId(), null,
                    AbsenceRequestStatus.APPROVED.name(), validationComment);
        }
        return request;
    }

    @Transactional
    public AbsenceRequestEntity reject(UUID id, String validationComment) {
        AbsenceRequestEntity request = get(id);
        request.setStatus(AbsenceRequestStatus.REJECTED);
        request.setValidationComment(validationComment);
        request.setValidatedBy(currentUser.memberIdOrSystem());
        request.setValidatedAt(Instant.now());
        notificationService.notifyInApp(request.getMemberId(), NotificationType.ABSENCE_REJECTED,
                "Absence refusée", "Votre demande d'absence a été refusée.", "{\"absenceRequestId\":\"" + request.getId() + "\"}");
        return request;
    }
}
