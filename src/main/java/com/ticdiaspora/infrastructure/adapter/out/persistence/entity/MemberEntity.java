package com.ticdiaspora.infrastructure.adapter.out.persistence.entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.ticdiaspora.domain.model.enums.ApplicationRole;
import com.ticdiaspora.domain.model.enums.ContributionType;
import com.ticdiaspora.domain.model.enums.MemberStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "members")
public class MemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;
    private String country;
    private String city;
    private String fullAddress;

    @Column(nullable = false)
    private LocalDate joinedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status = MemberStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationRole role = ApplicationRole.MEMBER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContributionType contributionType = ContributionType.XAF_50000;

    private String photoUrl;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String orangeMoneyNumber;
    private String orangeMoneyAccountName;
    private String mtnMoneyNumber;
    private String mtnMoneyAccountName;

    @Column(nullable = false)
    private int totalMeetingsChaired;

    private Instant lastChairedAt;

    @Column(nullable = false)
    private int annualAuthorizedAbsences;

    @Column(nullable = false)
    private int annualUnauthorizedAbsences;

    @Column(nullable = false)
    private long totalPenaltiesAmount;

    @Column(nullable = false)
    private long unpaidPenaltiesAmount;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

}
