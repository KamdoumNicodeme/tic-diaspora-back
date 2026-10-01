package com.ticdiaspora.application.port.in;

import com.ticdiaspora.domain.model.enums.ApplicationRole;
import com.ticdiaspora.domain.model.enums.ContributionType;

import java.time.LocalDate;

public record MemberCommand(
        String firstName,
        String lastName,
        String email,
        String phone,
        String country,
        String city,
        String fullAddress,
        LocalDate joinedAt,
        ApplicationRole role,
        ContributionType contributionType,
        String photoUrl,
        String emergencyContactName,
        String emergencyContactPhone,
        String orangeMoneyNumber,
        String orangeMoneyAccountName,
        String mtnMoneyNumber,
        String mtnMoneyAccountName,
        String temporaryPassword
) {
}
