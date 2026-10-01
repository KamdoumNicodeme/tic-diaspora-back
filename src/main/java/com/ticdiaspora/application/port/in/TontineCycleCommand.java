package com.ticdiaspora.application.port.in;

import java.util.UUID;

public record TontineCycleCommand(
        int month,
        int year,
        UUID beneficiaryId,
        UUID secondaryBeneficiaryId,
        String comment
) {
}
