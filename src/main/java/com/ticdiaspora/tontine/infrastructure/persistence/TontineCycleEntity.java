package com.ticdiaspora.tontine.infrastructure.persistence;

import com.ticdiaspora.shared.domain.enums.TontineCycleStatus;
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

    public UUID getId() {
        return id;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public UUID getBeneficiaryId() {
        return beneficiaryId;
    }

    public void setBeneficiaryId(UUID beneficiaryId) {
        this.beneficiaryId = beneficiaryId;
    }

    public TontineCycleStatus getStatus() {
        return status;
    }

    public void setStatus(TontineCycleStatus status) {
        this.status = status;
    }

    public long getExpectedAmount() {
        return expectedAmount;
    }

    public void setExpectedAmount(long expectedAmount) {
        this.expectedAmount = expectedAmount;
    }

    public long getCollectedAmount() {
        return collectedAmount;
    }

    public void setCollectedAmount(long collectedAmount) {
        this.collectedAmount = collectedAmount;
    }

    public long getTransferredAmount() {
        return transferredAmount;
    }

    public void setTransferredAmount(long transferredAmount) {
        this.transferredAmount = transferredAmount;
    }

    public long getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(long remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public Instant getOpenedAt() {
        return openedAt;
    }

    public void setOpenedAt(Instant openedAt) {
        this.openedAt = openedAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(Instant closedAt) {
        this.closedAt = closedAt;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
