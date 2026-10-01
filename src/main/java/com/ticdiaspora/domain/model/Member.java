package com.ticdiaspora.domain.model;

import com.ticdiaspora.domain.model.enums.ApplicationRole;
import com.ticdiaspora.domain.model.enums.ContributionType;
import com.ticdiaspora.domain.model.enums.MemberStatus;
import java.time.Instant;
import java.time.LocalDate;
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
public class Member {

    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String country;
    private String city;
    private String fullAddress;
    private LocalDate joinedAt;
    @Builder.Default
    private MemberStatus status = MemberStatus.ACTIVE;
    @Builder.Default
    private ApplicationRole role = ApplicationRole.MEMBER;
    @Builder.Default
    private ContributionType contributionType = ContributionType.XAF_50000;
    private String photoUrl;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String orangeMoneyNumber;
    private String orangeMoneyAccountName;
    private String mtnMoneyNumber;
    private String mtnMoneyAccountName;
    private int totalMeetingsChaired;
    private Instant lastChairedAt;
    private int annualAuthorizedAbsences;
    private int annualUnauthorizedAbsences;
    private long totalPenaltiesAmount;
    private long unpaidPenaltiesAmount;
    private Instant createdAt;
    private Instant updatedAt;
}
