package com.ticdiaspora.dashboard.infrastructure.web;

import com.ticdiaspora.attendance.infrastructure.persistence.AttendanceRecordJpaRepository;
import com.ticdiaspora.charity.infrastructure.persistence.CharityFundMovementJpaRepository;
import com.ticdiaspora.contribution.infrastructure.persistence.ContributionEntity;
import com.ticdiaspora.contribution.infrastructure.persistence.ContributionJpaRepository;
import com.ticdiaspora.decision.infrastructure.persistence.DecisionJpaRepository;
import com.ticdiaspora.meeting.infrastructure.persistence.MeetingEntity;
import com.ticdiaspora.meeting.infrastructure.persistence.MeetingJpaRepository;
import com.ticdiaspora.member.infrastructure.persistence.MemberEntity;
import com.ticdiaspora.member.infrastructure.persistence.MemberJpaRepository;
import com.ticdiaspora.penalty.infrastructure.persistence.PenaltyJpaRepository;
import com.ticdiaspora.project.infrastructure.persistence.ProjectJpaRepository;
import com.ticdiaspora.shared.domain.enums.AttendanceStatus;
import com.ticdiaspora.shared.domain.enums.ContributionStatus;
import com.ticdiaspora.shared.domain.enums.DecisionStatus;
import com.ticdiaspora.shared.domain.enums.MeetingStatus;
import com.ticdiaspora.shared.domain.enums.MemberStatus;
import com.ticdiaspora.shared.domain.enums.PenaltyStatus;
import com.ticdiaspora.shared.domain.enums.ProjectStatus;
import com.ticdiaspora.tontine.infrastructure.persistence.TontineCycleEntity;
import com.ticdiaspora.tontine.infrastructure.persistence.TontineCycleJpaRepository;
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

    private final MemberJpaRepository members;
    private final MeetingJpaRepository meetings;
    private final TontineCycleJpaRepository cycles;
    private final ContributionJpaRepository contributions;
    private final PenaltyJpaRepository penalties;
    private final CharityFundMovementJpaRepository charityMovements;
    private final AttendanceRecordJpaRepository attendanceRecords;
    private final ProjectJpaRepository projects;
    private final DecisionJpaRepository decisions;

    public DashboardController(MemberJpaRepository members, MeetingJpaRepository meetings, TontineCycleJpaRepository cycles,
                               ContributionJpaRepository contributions, PenaltyJpaRepository penalties,
                               CharityFundMovementJpaRepository charityMovements, AttendanceRecordJpaRepository attendanceRecords,
                               ProjectJpaRepository projects, DecisionJpaRepository decisions) {
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
        TontineCycleEntity currentCycle = cycles.findAll().stream()
                .max(Comparator.comparingInt(TontineCycleEntity::getYear).thenComparingInt(TontineCycleEntity::getMonth))
                .orElse(null);
        List<ContributionEntity> currentContributions = currentCycle == null ? List.of() : contributions.findAllByCycleId(currentCycle.getId());
        List<UUID> debtorMembers = currentContributions.stream()
                .filter(contribution -> contribution.getStatus() != ContributionStatus.PAID)
                .map(ContributionEntity::getMemberId)
                .toList();
        List<MemberRisk> closeToThreshold = members.findAll().stream()
                .filter(member -> member.getAnnualUnauthorizedAbsences() == 3)
                .map(member -> new MemberRisk(member.getId(), member.getFirstName(), member.getLastName(), member.getAnnualUnauthorizedAbsences()))
                .toList();
        List<MemberRisk> exceededThreshold = members.findAll().stream()
                .filter(member -> member.getAnnualUnauthorizedAbsences() > 4)
                .map(member -> new MemberRisk(member.getId(), member.getFirstName(), member.getLastName(), member.getAnnualUnauthorizedAbsences()))
                .toList();
        MeetingEntity nextMeeting = meetings.findFirstByMeetingDateGreaterThanEqualAndStatusOrderByMeetingDateAsc(LocalDate.now(), MeetingStatus.PLANNED)
                .orElse(null);
        long unauthorizedAbsences = attendanceRecords.findAll().stream()
                .filter(record -> record.getExpectedStartAt().getYear() == LocalDate.now().getYear())
                .filter(record -> record.getStatus() == AttendanceStatus.ABSENT_UNAUTHORIZED || record.getStatus() == AttendanceStatus.ABSENT_FROM_DELAY)
                .count();
        return new DashboardResponse(
                members.count(),
                members.countByStatus(MemberStatus.ACTIVE),
                members.countByStatus(MemberStatus.SUSPENDED),
                nextMeeting == null ? null : nextMeeting.getId(),
                nextMeeting == null ? null : nextMeeting.getMeetingDate().toString(),
                nextMeeting == null ? null : nextMeeting.getChairpersonId(),
                currentCycle == null ? null : currentCycle.getBeneficiaryId(),
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
            String nextMeetingDate,
            UUID plannedChairpersonId,
            UUID currentBeneficiaryId,
            long tontineExpectedAmount,
            long tontineCollectedAmount,
            long tontineRemainingAmount,
            List<UUID> debtorMemberIds,
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
}
