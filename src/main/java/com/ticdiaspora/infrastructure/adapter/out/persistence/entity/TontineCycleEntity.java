package com.ticdiaspora.infrastructure.adapter.out.persistence.entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.ticdiaspora.domain.model.enums.TontineCycleStatus;
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
@Table(name = "tontine_cycles")
public class TontineCycleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "cycle_month", nullable = false)
    private int month;

    @Column(name = "cycle_year", nullable = false)
    private int year;

    private UUID beneficiaryId;
    private UUID secondaryBeneficiaryId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TontineCycleStatus status = TontineCycleStatus.PLANNED;

    @Column(nullable = false)
    private long expectedAmount;

    @Column(nullable = false)
    private long collectedAmount;

    @Column(nullable = false)
    private long transferredAmount;

    @Column(nullable = false)
    private long remainingAmount;

    private Instant openedAt;
    private Instant closedAt;

    @Column(columnDefinition = "text")
    private String comment;

}
