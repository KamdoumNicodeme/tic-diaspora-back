package com.ticdiaspora.infrastructure.adapter.in.web;

import com.ticdiaspora.application.usecase.*;
import com.ticdiaspora.application.port.out.*;
import com.ticdiaspora.infrastructure.adapter.in.web.dto.*;

import com.ticdiaspora.application.port.out.CharityFundMovementRepositoryPort;
import com.ticdiaspora.application.port.out.MemberRepositoryPort;
import com.ticdiaspora.application.port.out.PenaltyRepositoryPort;
import com.ticdiaspora.domain.model.enums.PenaltyStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final MemberRepositoryPort members;
    private final PenaltyRepositoryPort penalties;
    private final CharityFundMovementRepositoryPort charityMovements;

    public ReportController(MemberRepositoryPort members, PenaltyRepositoryPort penalties, CharityFundMovementRepositoryPort charityMovements) {
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
