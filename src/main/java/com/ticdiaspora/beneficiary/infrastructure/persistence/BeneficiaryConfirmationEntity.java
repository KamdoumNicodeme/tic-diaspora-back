package com.ticdiaspora.beneficiary.infrastructure.persistence;

import com.ticdiaspora.shared.domain.enums.BeneficiaryConfirmationStatus;
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

    public UUID getId() {
        return id;
    }

    public UUID getCycleId() {
        return cycleId;
    }

    public void setCycleId(UUID cycleId) {
        this.cycleId = cycleId;
    }

    public UUID getBeneficiaryId() {
        return beneficiaryId;
    }

    public void setBeneficiaryId(UUID beneficiaryId) {
        this.beneficiaryId = beneficiaryId;
    }

    public long getExpectedAmount() {
        return expectedAmount;
    }

    public void setExpectedAmount(long expectedAmount) {
        this.expectedAmount = expectedAmount;
    }

    public long getReceivedAmount() {
        return receivedAmount;
    }

    public void setReceivedAmount(long receivedAmount) {
        this.receivedAmount = receivedAmount;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(Instant receivedAt) {
        this.receivedAt = receivedAt;
    }

    public BeneficiaryConfirmationStatus getStatus() {
        return status;
    }

    public void setStatus(BeneficiaryConfirmationStatus status) {
        this.status = status;
    }

    public String getBeneficiaryComment() {
        return beneficiaryComment;
    }

    public void setBeneficiaryComment(String beneficiaryComment) {
        this.beneficiaryComment = beneficiaryComment;
    }

    public UUID getFinalValidatedBy() {
        return finalValidatedBy;
    }

    public void setFinalValidatedBy(UUID finalValidatedBy) {
        this.finalValidatedBy = finalValidatedBy;
    }

    public Instant getFinalValidatedAt() {
        return finalValidatedAt;
    }

    public void setFinalValidatedAt(Instant finalValidatedAt) {
        this.finalValidatedAt = finalValidatedAt;
    }
}
