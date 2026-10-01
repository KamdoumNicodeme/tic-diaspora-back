package com.ticdiaspora.member.infrastructure.persistence;

import com.ticdiaspora.shared.domain.enums.ApplicationRole;
import com.ticdiaspora.shared.domain.enums.ContributionType;
import com.ticdiaspora.shared.domain.enums.MemberStatus;
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

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }

    public LocalDate getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDate joinedAt) {
        this.joinedAt = joinedAt;
    }

    public MemberStatus getStatus() {
        return status;
    }

    public void setStatus(MemberStatus status) {
        this.status = status;
    }

    public ApplicationRole getRole() {
        return role;
    }

    public void setRole(ApplicationRole role) {
        this.role = role;
    }

    public ContributionType getContributionType() {
        return contributionType;
    }

    public void setContributionType(ContributionType contributionType) {
        this.contributionType = contributionType;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getEmergencyContactName() {
        return emergencyContactName;
    }

    public void setEmergencyContactName(String emergencyContactName) {
        this.emergencyContactName = emergencyContactName;
    }

    public String getEmergencyContactPhone() {
        return emergencyContactPhone;
    }

    public void setEmergencyContactPhone(String emergencyContactPhone) {
        this.emergencyContactPhone = emergencyContactPhone;
    }

    public int getTotalMeetingsChaired() {
        return totalMeetingsChaired;
    }

    public void setTotalMeetingsChaired(int totalMeetingsChaired) {
        this.totalMeetingsChaired = totalMeetingsChaired;
    }

    public Instant getLastChairedAt() {
        return lastChairedAt;
    }

    public void setLastChairedAt(Instant lastChairedAt) {
        this.lastChairedAt = lastChairedAt;
    }

    public int getAnnualAuthorizedAbsences() {
        return annualAuthorizedAbsences;
    }

    public void setAnnualAuthorizedAbsences(int annualAuthorizedAbsences) {
        this.annualAuthorizedAbsences = annualAuthorizedAbsences;
    }

    public int getAnnualUnauthorizedAbsences() {
        return annualUnauthorizedAbsences;
    }

    public void setAnnualUnauthorizedAbsences(int annualUnauthorizedAbsences) {
        this.annualUnauthorizedAbsences = annualUnauthorizedAbsences;
    }

    public long getTotalPenaltiesAmount() {
        return totalPenaltiesAmount;
    }

    public void setTotalPenaltiesAmount(long totalPenaltiesAmount) {
        this.totalPenaltiesAmount = totalPenaltiesAmount;
    }

    public long getUnpaidPenaltiesAmount() {
        return unpaidPenaltiesAmount;
    }

    public void setUnpaidPenaltiesAmount(long unpaidPenaltiesAmount) {
        this.unpaidPenaltiesAmount = unpaidPenaltiesAmount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
