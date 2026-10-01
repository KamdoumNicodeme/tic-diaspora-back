package com.ticdiaspora.infrastructure.adapter.out.persistence.entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.ticdiaspora.domain.model.enums.ContributionStatus;
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
@Table(name = "contributions")
public class ContributionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID cycleId;

    @Column(nullable = false)
    private UUID memberId;

    @Column(nullable = false)
    private long expectedAmount;

    @Column(nullable = false)
    private long paidAmount;

    @Column(nullable = false)
    private String currency = "XAF";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContributionStatus status = ContributionStatus.PENDING;

    private Instant paidAt;
    private UUID validatedBy;
    private Instant validatedAt;

}
