package com.ticdiaspora.domain.model;

import com.ticdiaspora.domain.model.enums.BeneficiaryConfirmationStatus;
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
public class BeneficiaryConfirmation {

    private UUID id;
    private UUID cycleId;
    private UUID beneficiaryId;
    private long expectedAmount;
    private long receivedAmount;
    private Instant receivedAt;
    @Builder.Default
    private BeneficiaryConfirmationStatus status = BeneficiaryConfirmationStatus.PENDING;
    private String beneficiaryComment;
    private UUID finalValidatedBy;
    private Instant finalValidatedAt;
}
