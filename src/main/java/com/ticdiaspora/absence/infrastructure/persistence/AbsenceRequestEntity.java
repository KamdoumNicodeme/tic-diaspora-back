package com.ticdiaspora.absence.infrastructure.persistence;

import com.ticdiaspora.shared.domain.enums.AbsenceRequestStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "absence_requests")
public class AbsenceRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID memberId;

    @Column(nullable = false)
    private UUID meetingId;

    @Column(nullable = false, columnDefinition = "text")
    private String reason;

    @Column(nullable = false)
    private Instant requestedAt;

    @Column(nullable = false)
    private long hoursBeforeMeeting;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AbsenceRequestStatus status;

    @Column(columnDefinition = "text")
    private String validationComment;

    private UUID validatedBy;
    private Instant validatedAt;

    @Column(nullable = false)
    private boolean exceptionalApproval;

    public UUID getId() {
        return id;
    }

    public UUID getMemberId() {
        return memberId;
    }

    public void setMemberId(UUID memberId) {
        this.memberId = memberId;
    }

    public UUID getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(UUID meetingId) {
        this.meetingId = meetingId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(Instant requestedAt) {
        this.requestedAt = requestedAt;
    }

    public long getHoursBeforeMeeting() {
        return hoursBeforeMeeting;
    }

    public void setHoursBeforeMeeting(long hoursBeforeMeeting) {
        this.hoursBeforeMeeting = hoursBeforeMeeting;
    }

    public AbsenceRequestStatus getStatus() {
        return status;
    }

    public void setStatus(AbsenceRequestStatus status) {
        this.status = status;
    }

    public String getValidationComment() {
        return validationComment;
    }

    public void setValidationComment(String validationComment) {
        this.validationComment = validationComment;
    }

    public UUID getValidatedBy() {
        return validatedBy;
    }

    public void setValidatedBy(UUID validatedBy) {
        this.validatedBy = validatedBy;
    }

    public Instant getValidatedAt() {
        return validatedAt;
    }

    public void setValidatedAt(Instant validatedAt) {
        this.validatedAt = validatedAt;
    }

    public boolean isExceptionalApproval() {
        return exceptionalApproval;
    }

    public void setExceptionalApproval(boolean exceptionalApproval) {
        this.exceptionalApproval = exceptionalApproval;
    }
}
