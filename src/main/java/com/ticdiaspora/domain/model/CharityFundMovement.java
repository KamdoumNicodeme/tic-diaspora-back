package com.ticdiaspora.domain.model;

import com.ticdiaspora.domain.model.enums.CharityMovementType;
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
public class CharityFundMovement {

    private UUID id;
    private UUID memberId;
    private UUID penaltyId;
    private UUID projectId;
    private CharityMovementType type;
    private long amount;
    private String description;
    private Instant movementDate;
    private UUID createdBy;
}
