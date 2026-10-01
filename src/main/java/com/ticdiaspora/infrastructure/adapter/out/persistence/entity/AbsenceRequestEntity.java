package com.ticdiaspora.infrastructure.adapter.out.persistence.entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.ticdiaspora.domain.model.enums.AbsenceRequestStatus;
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

@Getter
@Setter
@NoArgsConstructor
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

}
