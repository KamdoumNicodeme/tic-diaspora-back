package com.ticdiaspora.domain.model;

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
public class ContributionPayment {

    private UUID id;
    private UUID contributionId;
    private long amount;
    private PaymentMethod method;
    private String transactionReference;
    private String proofUrl;
    private Instant paidAt;
    private UUID validatedBy;
    private Instant validatedAt;
}
