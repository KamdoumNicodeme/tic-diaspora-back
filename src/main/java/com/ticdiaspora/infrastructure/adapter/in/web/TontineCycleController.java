package com.ticdiaspora.infrastructure.adapter.in.web;
import com.ticdiaspora.infrastructure.adapter.in.web.mapper.ContributionWebMapper;
import com.ticdiaspora.infrastructure.adapter.in.web.mapper.TontineCycleWebMapper;

import com.ticdiaspora.application.usecase.*;
import com.ticdiaspora.application.port.out.*;
import com.ticdiaspora.infrastructure.adapter.in.web.dto.*;

import com.ticdiaspora.domain.model.Member;
import com.ticdiaspora.domain.model.Contribution;
import com.ticdiaspora.domain.model.TontineCycle;
import com.ticdiaspora.infrastructure.adapter.in.web.dto.ContributionDtos;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tontine-cycles")
public class TontineCycleController {

    private final TontineCycleService tontineCycleService;
    private final ContributionService contributionService;
    private final MemberRepositoryPort members;
    private final TontineCycleWebMapper tontineCycleMapper;
    private final ContributionWebMapper contributionMapper;

    public TontineCycleController(TontineCycleService tontineCycleService, ContributionService contributionService,
                                  MemberRepositoryPort members,
                                  TontineCycleWebMapper tontineCycleMapper, ContributionWebMapper contributionMapper) {
        this.tontineCycleService = tontineCycleService;
        this.contributionService = contributionService;
        this.members = members;
        this.tontineCycleMapper = tontineCycleMapper;
        this.contributionMapper = contributionMapper;
    }

    @GetMapping
    Page<TontineDtos.TontineCycleResponse> list(Pageable pageable) {
        return tontineCycleService.list(pageable).map(this::toResponse);
    }

    @PostMapping
    @PreAuthorize("hasRole('PRESIDENT')")
    TontineDtos.TontineCycleResponse create(@Valid @RequestBody TontineDtos.TontineCycleRequest request) {
        return toResponse(tontineCycleService.create(tontineCycleMapper.toCommand(request)));
    }

    @GetMapping("/{id}")
    TontineDtos.TontineCycleResponse get(@PathVariable UUID id) {
        return toResponse(tontineCycleService.get(id));
    }

    @GetMapping("/{id}/contributions")
    List<ContributionDtos.ContributionResponse> contributions(@PathVariable UUID id) {
        return contributionService.byCycle(id).stream().map(this::toContributionResponse).toList();
    }

    @PatchMapping("/{id}/open")
    @PreAuthorize("hasRole('PRESIDENT')")
    TontineDtos.TontineCycleResponse open(@PathVariable UUID id) {
        return toResponse(tontineCycleService.open(id));
    }

    @PatchMapping("/{id}/change-beneficiary")
    @PreAuthorize("hasRole('PRESIDENT')")
    TontineDtos.TontineCycleResponse changeBeneficiary(@PathVariable UUID id, @Valid @RequestBody TontineDtos.ChangeBeneficiaryRequest request) {
        return toResponse(tontineCycleService.changeBeneficiary(id, request.beneficiaryId(), request.secondaryBeneficiaryId(), request.unanimousAgreement(), request.reason()));
    }

    @PatchMapping("/{id}/close")
    @PreAuthorize("hasRole('PRESIDENT')")
    TontineDtos.TontineCycleResponse close(@PathVariable UUID id) {
        return toResponse(tontineCycleService.close(id));
    }

    private TontineDtos.TontineCycleResponse toResponse(TontineCycle cycle) {
        Member beneficiary = cycle.getBeneficiaryId() == null ? null : members.findById(cycle.getBeneficiaryId()).orElse(null);
        Member secondaryBeneficiary = cycle.getSecondaryBeneficiaryId() == null ? null : members.findById(cycle.getSecondaryBeneficiaryId()).orElse(null);
        return new TontineDtos.TontineCycleResponse(
                cycle.getId(),
                cycle.getMonth(),
                cycle.getYear(),
                cycle.getBeneficiaryId(),
                beneficiary == null ? null : beneficiary.getFirstName() + " " + beneficiary.getLastName(),
                beneficiary == null ? null : beneficiary.getOrangeMoneyNumber(),
                beneficiary == null ? null : beneficiary.getOrangeMoneyAccountName(),
                beneficiary == null ? null : beneficiary.getMtnMoneyNumber(),
                beneficiary == null ? null : beneficiary.getMtnMoneyAccountName(),
                primaryExpectedAmount(cycle),
                cycle.getSecondaryBeneficiaryId(),
                secondaryBeneficiary == null ? null : secondaryBeneficiary.getFirstName() + " " + secondaryBeneficiary.getLastName(),
                secondaryBeneficiary == null ? null : secondaryBeneficiary.getOrangeMoneyNumber(),
                secondaryBeneficiary == null ? null : secondaryBeneficiary.getOrangeMoneyAccountName(),
                secondaryBeneficiary == null ? null : secondaryBeneficiary.getMtnMoneyNumber(),
                secondaryBeneficiary == null ? null : secondaryBeneficiary.getMtnMoneyAccountName(),
                secondaryExpectedAmount(cycle),
                cycle.getStatus(),
                cycle.getExpectedAmount(),
                cycle.getCollectedAmount(),
                cycle.getTransferredAmount(),
                cycle.getRemainingAmount(),
                cycle.getOpenedAt(),
                cycle.getClosedAt(),
                cycle.getComment()
        );
    }

    private long primaryExpectedAmount(TontineCycle cycle) {
        return cycle.getSecondaryBeneficiaryId() == null ? cycle.getExpectedAmount() : cycle.getExpectedAmount() / 2;
    }

    private long secondaryExpectedAmount(TontineCycle cycle) {
        return cycle.getSecondaryBeneficiaryId() == null ? 0 : cycle.getExpectedAmount() / 2;
    }

    private ContributionDtos.ContributionResponse toContributionResponse(Contribution contribution) {
        ContributionDtos.ContributionResponse response = contributionMapper.toResponse(contribution);
        Member member = members.findById(contribution.getMemberId()).orElse(null);
        return new ContributionDtos.ContributionResponse(
                response.id(),
                response.cycleId(),
                response.memberId(),
                member == null ? null : member.getFirstName() + " " + member.getLastName(),
                response.expectedAmount(),
                response.paidAmount(),
                response.remainingAmount(),
                response.currency(),
                response.status(),
                response.paidAt(),
                response.validatedBy(),
                response.validatedAt(),
                response.lastPaymentMethod(),
                response.lastTransactionReference(),
                response.lastProofUrl(),
                response.lastPaymentAt()
        );
    }
}
