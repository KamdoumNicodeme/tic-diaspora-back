package com.ticdiaspora.infrastructure.adapter.in.web;

import com.ticdiaspora.application.usecase.*;
import com.ticdiaspora.application.port.out.*;
import com.ticdiaspora.infrastructure.adapter.in.web.dto.*;

import com.ticdiaspora.application.port.out.AttendanceRecordRepositoryPort;
import com.ticdiaspora.application.port.out.CharityFundMovementRepositoryPort;
import com.ticdiaspora.domain.model.Contribution;
import com.ticdiaspora.application.port.out.ContributionRepositoryPort;
import com.ticdiaspora.application.port.out.DecisionRepositoryPort;
import com.ticdiaspora.domain.model.Meeting;
import com.ticdiaspora.application.port.out.MeetingRepositoryPort;
import com.ticdiaspora.domain.model.Member;
import com.ticdiaspora.application.port.out.MemberRepositoryPort;
import com.ticdiaspora.application.port.out.PenaltyRepositoryPort;
import com.ticdiaspora.application.port.out.ProjectRepositoryPort;
import com.ticdiaspora.domain.model.enums.AttendanceStatus;
import com.ticdiaspora.domain.model.enums.ContributionStatus;
import com.ticdiaspora.domain.model.enums.DecisionStatus;
import com.ticdiaspora.domain.model.enums.MeetingStatus;
import com.ticdiaspora.domain.model.enums.MemberStatus;
import com.ticdiaspora.domain.model.enums.PenaltyStatus;
import com.ticdiaspora.domain.model.enums.ProjectStatus;
import com.ticdiaspora.domain.model.TontineCycle;
import com.ticdiaspora.application.port.out.TontineCycleRepositoryPort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final MemberRepositoryPort members;
    private final MeetingRepositoryPort meetings;
    private final TontineCycleRepositoryPort cycles;
    private final ContributionRepositoryPort contributions;
    private final PenaltyRepositoryPort penalties;
    private final CharityFundMovementRepositoryPort charityMovements;
    private final AttendanceRecordRepositoryPort attendanceRecords;
    private final ProjectRepositoryPort projects;
    private final DecisionRepositoryPort decisions;

    public DashboardController(MemberRepositoryPort members, MeetingRepositoryPort meetings, TontineCycleRepositoryPort cycles,
                               ContributionRepositoryPort contributions, PenaltyRepositoryPort penalties,
                               CharityFundMovementRepositoryPort charityMovements, AttendanceRecordRepositoryPort attendanceRecords,
                               ProjectRepositoryPort projects, DecisionRepositoryPort decisions) {
        this.members = members;
        this.meetings = meetings;
        this.cycles = cycles;
        this.contributions = contributions;
        this.penalties = penalties;
        this.charityMovements = charityMovements;
        this.attendanceRecords = attendanceRecords;
        this.projects = projects;
        this.decisions = decisions;
    }

    @GetMapping
    DashboardResponse summary() {
        TontineCycle currentCycle = cycles.findAll().stream()
                .max(Comparator.comparingInt(TontineCycle::getYear).thenComparingInt(TontineCycle::getMonth))
                .orElse(null);
        List<Contribution> currentContributions = currentCycle == null ? List.of() : contributions.findAllByCycleId(currentCycle.getId());
        List<MemberSummary> debtorMembers = currentContributions.stream()
                .filter(contribution -> contribution.getStatus() != ContributionStatus.PAID)
                .map(Contribution::getMemberId)
                .map(this::memberSummary)
                .toList();
        List<MemberRisk> closeToThreshold = members.findAll().stream()
                .filter(member -> member.getAnnualUnauthorizedAbsences() == 3)
                .map(member -> new MemberRisk(member.getId(), member.getFirstName(), member.getLastName(), member.getAnnualUnauthorizedAbsences()))
                .toList();
        List<MemberRisk> exceededThreshold = members.findAll().stream()
                .filter(member -> member.getAnnualUnauthorizedAbsences() > 4)
                .map(member -> new MemberRisk(member.getId(), member.getFirstName(), member.getLastName(), member.getAnnualUnauthorizedAbsences()))
                .toList();
        Meeting nextMeeting = meetings.findFirstByMeetingDateGreaterThanEqualAndStatusOrderByMeetingDateAsc(LocalDate.now(), MeetingStatus.PLANNED)
                .orElse(null);
        Member nextChairperson = nextMeeting == null || nextMeeting.getChairpersonId() == null
                ? null
                : members.findById(nextMeeting.getChairpersonId()).orElse(null);
        Member currentBeneficiary = currentCycle == null || currentCycle.getBeneficiaryId() == null
                ? null
                : members.findById(currentCycle.getBeneficiaryId()).orElse(null);
        Member secondaryBeneficiary = currentCycle == null || currentCycle.getSecondaryBeneficiaryId() == null
                ? null
                : members.findById(currentCycle.getSecondaryBeneficiaryId()).orElse(null);
        long unauthorizedAbsences = attendanceRecords.findAll().stream()
                .filter(record -> record.getExpectedStartAt().getYear() == LocalDate.now().getYear())
                .filter(record -> record.getStatus() == AttendanceStatus.ABSENT_UNAUTHORIZED || record.getStatus() == AttendanceStatus.ABSENT_FROM_DELAY)
                .count();
        return new DashboardResponse(
                members.count(),
                members.countByStatus(MemberStatus.ACTIVE),
                members.countByStatus(MemberStatus.SUSPENDED),
                nextMeeting == null ? null : nextMeeting.getId(),
                nextMeeting == null ? null : nextMeeting.getTitle(),
                nextMeeting == null ? null : nextMeeting.getMeetingDate().toString(),
                nextMeeting == null ? null : nextMeeting.getChairpersonId(),
                nextChairperson == null ? null : nextChairperson.getFirstName() + " " + nextChairperson.getLastName(),
                currentCycle == null ? null : currentCycle.getBeneficiaryId(),
                currentBeneficiary == null ? null : currentBeneficiary.getFirstName() + " " + currentBeneficiary.getLastName(),
                currentCycle == null ? null : currentCycle.getSecondaryBeneficiaryId(),
                secondaryBeneficiary == null ? null : secondaryBeneficiary.getFirstName() + " " + secondaryBeneficiary.getLastName(),
                currentCycle == null ? 0 : currentCycle.getExpectedAmount(),
                currentCycle == null ? 0 : currentCycle.getCollectedAmount(),
                currentCycle == null ? 0 : currentCycle.getRemainingAmount(),
                debtorMembers,
                penalties.sumAmountByStatus(PenaltyStatus.PENDING),
                charityMovements.balance(),
                unauthorizedAbsences,
                closeToThreshold,
                exceededThreshold,
                projects.countByStatus(ProjectStatus.IN_PROGRESS),
                decisions.countByStatusIn(List.of(DecisionStatus.OPEN, DecisionStatus.IN_PROGRESS))
        );
    }

    public record DashboardResponse(
            long totalMembers,
            long activeMembers,
            long suspendedMembers,
            UUID nextMeetingId,
            String nextMeetingTitle,
            String nextMeetingDate,
            UUID plannedChairpersonId,
            String plannedChairpersonFullName,
            UUID currentBeneficiaryId,
            String currentBeneficiaryFullName,
            UUID secondaryBeneficiaryId,
            String secondaryBeneficiaryFullName,
            long tontineExpectedAmount,
            long tontineCollectedAmount,
            long tontineRemainingAmount,
            List<MemberSummary> debtorMembers,
            long pendingPenaltiesAmount,
            long charityFundBalance,
            long unauthorizedAbsencesThisYear,
            List<MemberRisk> closeToAbsenceThreshold,
            List<MemberRisk> exceededAbsenceThreshold,
            long ongoingProjects,
            long openDecisions
    ) {
    }

    public record MemberRisk(UUID memberId, String firstName, String lastName, int unauthorizedAbsences) {
    }

    public record MemberSummary(UUID memberId, String firstName, String lastName) {
    }

    private MemberSummary memberSummary(UUID memberId) {
        return members.findById(memberId)
                .map(member -> new MemberSummary(member.getId(), member.getFirstName(), member.getLastName()))
                .orElse(new MemberSummary(memberId, "Membre", "inconnu"));
    }
}
