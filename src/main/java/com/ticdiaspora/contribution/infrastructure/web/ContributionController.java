package com.ticdiaspora.contribution.infrastructure.web;

import com.ticdiaspora.contribution.infrastructure.persistence.ContributionEntity;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/contributions")
public class ContributionController {

    private final ContributionService contributionService;

    public ContributionController(ContributionService contributionService) {
        this.contributionService = contributionService;
    }

    @GetMapping
    @PreAuthorize("hasRole('PRESIDENT')")
    Page<ContributionDtos.ContributionResponse> list(Pageable pageable) {
        return contributionService.list(pageable).map(this::toResponse);
    }

    @PostMapping
    @PreAuthorize("hasRole('PRESIDENT')")
    ContributionDtos.ContributionResponse recordPayment(@Valid @RequestBody ContributionDtos.ContributionPaymentRequest request) {
        return toResponse(contributionService.recordPayment(request.cycleId(), request.memberId(), request.amount(),
                request.method(), request.transactionReference(), request.proofUrl()));
    }

    @PatchMapping("/{id}/validate")
    @PreAuthorize("hasRole('PRESIDENT')")
    ContributionDtos.ContributionResponse validate(@PathVariable UUID id) {
        return toResponse(contributionService.validate(id));
    }

    private ContributionDtos.ContributionResponse toResponse(ContributionEntity contribution) {
        long remaining = Math.max(0, contribution.getExpectedAmount() - contribution.getPaidAmount());
        return new ContributionDtos.ContributionResponse(
                contribution.getId(), contribution.getCycleId(), contribution.getMemberId(), contribution.getExpectedAmount(),
                contribution.getPaidAmount(), remaining, contribution.getCurrency(), contribution.getStatus(),
                contribution.getPaidAt(), contribution.getValidatedBy(), contribution.getValidatedAt()
        );
    }
}
