package com.ticdiaspora.dashboard.infrastructure.web;

import com.ticdiaspora.charity.infrastructure.persistence.CharityFundMovementJpaRepository;
import com.ticdiaspora.member.infrastructure.persistence.MemberJpaRepository;
import com.ticdiaspora.penalty.infrastructure.persistence.PenaltyJpaRepository;
import com.ticdiaspora.shared.domain.enums.PenaltyStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final MemberJpaRepository members;
    private final PenaltyJpaRepository penalties;
    private final CharityFundMovementJpaRepository charityMovements;

    public ReportController(MemberJpaRepository members, PenaltyJpaRepository penalties, CharityFundMovementJpaRepository charityMovements) {
        this.members = members;
        this.penalties = penalties;
        this.charityMovements = charityMovements;
    }

    @GetMapping("/annual/{year}")
    @PreAuthorize("hasRole('PRESIDENT')")
    AnnualReport annual(@PathVariable int year) {
        return new AnnualReport(year, members.count(), penalties.sumAmountByStatus(PenaltyStatus.PAID), charityMovements.balance());
    }

    public record AnnualReport(int year, long membersCount, long paidPenaltiesAmount, long charityFundBalance) {
    }
}
