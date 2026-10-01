package com.ticdiaspora.application.port.in;

public record MemberProfileCommand(
        String firstName,
        String lastName,
        String phone,
        String country,
        String city,
        String fullAddress,
        String photoUrl,
        String emergencyContactName,
        String emergencyContactPhone,
        String orangeMoneyNumber,
        String orangeMoneyAccountName,
        String mtnMoneyNumber,
        String mtnMoneyAccountName
) {
}
