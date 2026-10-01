package com.ticdiaspora.domain.model;

import com.ticdiaspora.domain.model.enums.TontineCycleStatus;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class TontineCycle {

    private UUID id;
    private int month;
    private int year;
    private UUID beneficiaryId;
    private UUID secondaryBeneficiaryId;
    @Builder.Default
    private TontineCycleStatus status = TontineCycleStatus.PLANNED;
    private long expectedAmount;
    private long collectedAmount;
    private long transferredAmount;
    private long remainingAmount;
    private Instant openedAt;
    private Instant closedAt;
    private String comment;
}
