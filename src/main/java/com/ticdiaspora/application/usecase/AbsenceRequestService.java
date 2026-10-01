package com.ticdiaspora.application.usecase;
import com.ticdiaspora.application.port.in.*;
import com.ticdiaspora.application.port.out.*;

import com.ticdiaspora.domain.model.AbsenceClassification;
import com.ticdiaspora.domain.service.AbsenceRules;
import com.ticdiaspora.domain.model.AbsenceRequest;
import com.ticdiaspora.application.port.out.AbsenceRequestRepositoryPort;
import com.ticdiaspora.application.port.out.CurrentUserPort;
import com.ticdiaspora.domain.model.Meeting;
import com.ticdiaspora.application.port.out.MeetingRepositoryPort;
import com.ticdiaspora.application.port.out.MemberRepositoryPort;
import com.ticdiaspora.domain.model.Member;
import com.ticdiaspora.domain.model.enums.AbsenceRequestStatus;
import com.ticdiaspora.domain.model.enums.ApplicationRole;
import com.ticdiaspora.domain.model.enums.AuditAction;
import com.ticdiaspora.domain.model.enums.MemberStatus;
import com.ticdiaspora.domain.model.enums.NotificationType;
import com.ticdiaspora.domain.exception.BusinessException;
import com.ticdiaspora.domain.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.ticdiaspora.application.annotation.UseCase;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@UseCase
public class AbsenceRequestService {

    private final AbsenceRequestRepositoryPort absenceRequests;
    private final MeetingRepositoryPort meetings;
    private final MemberRepositoryPort members;
    private final AbsenceRules absenceRules;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final CurrentUserPort currentUser;

    public AbsenceRequestService(
            AbsenceRequestRepositoryPort absenceRequests,
            MeetingRepositoryPort meetings,
            MemberRepositoryPort members,
            AbsenceRules absenceRules,
            NotificationService notificationService,
            AuditService auditService,
            CurrentUserPort currentUser
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
    public Page<AbsenceRequest> list(Pageable pageable) {
        return absenceRequests.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public AbsenceRequest get(UUID id) {
        return absenceRequests.findById(id).orElseThrow(() -> new NotFoundException("Demande d'absence", id));
    }

    @Transactional(readOnly = true)
    public List<AbsenceRequest> mine() {
        UUID memberId = currentUser.memberIdOrSystem();
        if (memberId == null) {
            throw new BusinessException("AUTHENTICATED_MEMBER_REQUIRED", "Un membre connecté est requis");
        }
        return absenceRequests.findAllByMemberIdOrderByRequestedAtDesc(memberId);
    }

    @Transactional
    public AbsenceRequest create(UUID memberId, UUID meetingId, String reason) {
        Member requester = members.findById(memberId).orElseThrow(() -> new NotFoundException("Membre", memberId));
        Meeting meeting = meetings.findById(meetingId).orElseThrow(() -> new NotFoundException("Réunion", meetingId));
        Instant requestedAt = Instant.now();
        Instant meetingStartAt = meeting.getMeetingDate()
                .atTime(meeting.getPlannedStartTime())
                .atZone(ZoneId.systemDefault())
                .toInstant();
        AbsenceClassification classification = absenceRules.classify(requestedAt, meetingStartAt);
        AbsenceRequest absenceRequest = new AbsenceRequest();
        absenceRequest.setMemberId(memberId);
        absenceRequest.setMeetingId(meetingId);
        absenceRequest.setReason(reason);
        absenceRequest.setRequestedAt(requestedAt);
        absenceRequest.setHoursBeforeMeeting(classification.hoursBeforeMeeting());
        absenceRequest.setStatus(classification.status());
        if (classification.status() == AbsenceRequestStatus.AUTO_APPROVED) {
            absenceRequest.setValidatedAt(requestedAt);
        }
        absenceRequest = absenceRequests.save(absenceRequest);
        sendAbsenceRequestSubmittedNotifications(absenceRequest, requester, meeting);
        if (classification.status() == AbsenceRequestStatus.AUTO_APPROVED) {
            sendAbsenceDecisionNotifications(absenceRequest, true, "Votre absence a été automatiquement approuvée.");
        }
        return absenceRequest;
    }

    @Transactional
    public AbsenceRequest approve(UUID id, String validationComment, boolean exceptionalApproval) {
        AbsenceRequest request = get(id);
        if (request.getStatus() == AbsenceRequestStatus.LATE_REQUEST
                && !absenceRules.canApproveLateRequest(exceptionalApproval, validationComment)) {
            throw new BusinessException("LATE_ABSENCE_REQUIRES_EXCEPTION", "Une demande tardive exige une validation exceptionnelle avec commentaire");
        }
        request.setStatus(AbsenceRequestStatus.APPROVED);
        request.setValidationComment(validationComment);
        request.setExceptionalApproval(exceptionalApproval);
        request.setValidatedBy(currentUser.memberIdOrSystem());
        request.setValidatedAt(Instant.now());
        request = absenceRequests.save(request);
        sendAbsenceDecisionNotifications(request, true, "Votre demande d'absence a été approuvée.");
        if (exceptionalApproval) {
            auditService.record(AuditAction.ABSENCE_EXCEPTIONALLY_APPROVED, "AbsenceRequest", request.getId(), null,
                    AbsenceRequestStatus.APPROVED.name(), validationComment);
        }
        return request;
    }

    @Transactional
    public AbsenceRequest reject(UUID id, String validationComment) {
        AbsenceRequest request = get(id);
        request.setStatus(AbsenceRequestStatus.REJECTED);
        request.setValidationComment(validationComment);
        request.setValidatedBy(currentUser.memberIdOrSystem());
        request.setValidatedAt(Instant.now());
        request = absenceRequests.save(request);
        sendAbsenceDecisionNotifications(request, false, "Votre demande d'absence a été refusée.");
        return request;
    }

    private void sendAbsenceRequestSubmittedNotifications(AbsenceRequest request, Member requester, Meeting meeting) {
        String title = "Nouvelle demande d'absence";
        String requesterName = requester.getFirstName() + " " + requester.getLastName();
        String message = "%s a demandé une absence pour la réunion %s du %s.".formatted(
                requesterName,
                meeting.getTitle(),
                meeting.getMeetingDate()
        );
        String payload = "{\"absenceRequestId\":\"" + request.getId()
                + "\",\"meetingId\":\"" + request.getMeetingId()
                + "\",\"memberId\":\"" + request.getMemberId()
                + "\",\"status\":\"" + request.getStatus() + "\"}";

        members.findAll().stream()
                .filter(member -> member.getRole() == ApplicationRole.PRESIDENT)
                .filter(member -> member.getStatus() == MemberStatus.ACTIVE)
                .filter(member -> member.getEmail() != null && !member.getEmail().isBlank())
                .forEach(president -> {
                    notificationService.notifyInApp(
                            president.getId(),
                            NotificationType.ABSENCE_REQUEST_SUBMITTED,
                            title,
                            message,
                            payload
                    );
                    notificationService.notifyEmail(
                            president.getId(),
                            president.getEmail(),
                            NotificationType.ABSENCE_REQUEST_SUBMITTED,
                            title,
                            buildAbsenceRequestSubmittedEmail(president, requesterName, request, meeting),
                            payload
                    );
                });
    }

    private void sendAbsenceDecisionNotifications(AbsenceRequest request, boolean approved, String message) {
        String title = approved ? "Absence approuvée" : "Absence refusée";
        NotificationType type = approved ? NotificationType.ABSENCE_APPROVED : NotificationType.ABSENCE_REJECTED;
        String payload = "{\"absenceRequestId\":\"" + request.getId() + "\",\"meetingId\":\"" + request.getMeetingId() + "\"}";
        notificationService.notifyInApp(request.getMemberId(), type, title, message, payload);
        members.findById(request.getMemberId()).ifPresent(member ->
                notificationService.notifyEmail(member.getId(), member.getEmail(), type, title,
                        buildAbsenceDecisionEmail(member.getFirstName(), request, approved, message), payload)
        );
    }

    private String buildAbsenceDecisionEmail(String firstName, AbsenceRequest request, boolean approved, String message) {
        return """
                Bonjour %s,

                %s

                Réunion concernée : %s
                Motif déclaré : %s
                Statut : %s

                TIC Diaspora
                """.formatted(
                firstName,
                message,
                request.getMeetingId(),
                request.getReason(),
                approved ? "APPROUVEE" : "REFUSEE"
        );
    }

    private String buildAbsenceRequestSubmittedEmail(Member president, String requesterName, AbsenceRequest request, Meeting meeting) {
        return """
                Bonjour %s,

                Une nouvelle demande d'absence vient d'être déclarée dans TIC Diaspora.

                Membre : %s
                Réunion : %s
                Date : %s
                Heure : %s - %s
                Motif : %s
                Délai avant réunion : %s heure(s)
                Statut initial : %s

                Connectez-vous à la plateforme pour approuver ou rejeter la demande.

                TIC Diaspora
                """.formatted(
                president.getFirstName(),
                requesterName,
                meeting.getTitle(),
                meeting.getMeetingDate(),
                meeting.getPlannedStartTime(),
                meeting.getPlannedEndTime(),
                request.getReason(),
                request.getHoursBeforeMeeting(),
                request.getStatus()
        );
    }
}
