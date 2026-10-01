package com.ticdiaspora.domain.model;

import com.ticdiaspora.domain.model.enums.ContributionStatus;
import com.ticdiaspora.domain.model.enums.PaymentMethod;
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
public class Contribution {

    private UUID id;
    private UUID cycleId;
    private UUID memberId;
    private long expectedAmount;
    private long paidAmount;
    @Builder.Default
    private String currency = "XAF";
    @Builder.Default
    private ContributionStatus status = ContributionStatus.PENDING;
    private Instant paidAt;
    private UUID validatedBy;
    private Instant validatedAt;
    private PaymentMethod lastPaymentMethod;
    private String lastTransactionReference;
    private String lastProofUrl;
    private Instant lastPaymentAt;
}
