package com.ticdiaspora.infrastructure.adapter.out.persistence.entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.ticdiaspora.domain.model.enums.BeneficiaryConfirmationStatus;
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
@Table(name = "beneficiary_confirmations")
public class BeneficiaryConfirmationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID cycleId;

    @Column(nullable = false)
    private UUID beneficiaryId;

    @Column(nullable = false)
    private long expectedAmount;

    @Column(nullable = false)
    private long receivedAmount;

    private Instant receivedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BeneficiaryConfirmationStatus status = BeneficiaryConfirmationStatus.PENDING;

    @Column(columnDefinition = "text")
    private String beneficiaryComment;

    private UUID finalValidatedBy;
    private Instant finalValidatedAt;

}
