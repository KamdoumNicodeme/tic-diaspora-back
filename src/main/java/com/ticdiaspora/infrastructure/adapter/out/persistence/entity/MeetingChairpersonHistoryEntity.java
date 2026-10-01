package com.ticdiaspora.infrastructure.adapter.out.persistence.entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "meeting_chairperson_history")
public class MeetingChairpersonHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID meetingId;

    @Column(nullable = false)
    private UUID memberId;

    @Column(nullable = false)
    private String assignmentType;

    private UUID assignedBy;
    private String reason;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant assignedAt;

    private Instant completedAt;

}
