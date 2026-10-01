package com.ticdiaspora.tontine.infrastructure.web;

import com.ticdiaspora.contribution.infrastructure.persistence.ContributionEntity;
import com.ticdiaspora.contribution.infrastructure.persistence.ContributionJpaRepository;
import com.ticdiaspora.contribution.infrastructure.web.ContributionDtos;
import com.ticdiaspora.tontine.infrastructure.persistence.TontineCycleEntity;
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
    private final ContributionJpaRepository contributions;

    public TontineCycleController(TontineCycleService tontineCycleService, ContributionJpaRepository contributions) {
        this.tontineCycleService = tontineCycleService;
        this.contributions = contributions;
    }

    @GetMapping
    Page<TontineDtos.TontineCycleResponse> list(Pageable pageable) {
        return tontineCycleService.list(pageable).map(this::toResponse);
    }

    @PostMapping
    @PreAuthorize("hasRole('PRESIDENT')")
    TontineDtos.TontineCycleResponse create(@Valid @RequestBody TontineDtos.TontineCycleRequest request) {
        return toResponse(tontineCycleService.create(request));
    }

    @GetMapping("/{id}")
    TontineDtos.TontineCycleResponse get(@PathVariable UUID id) {
        return toResponse(tontineCycleService.get(id));
    }

    @GetMapping("/{id}/contributions")
    List<ContributionDtos.ContributionResponse> contributions(@PathVariable UUID id) {
        return contributions.findAllByCycleId(id).stream().map(this::toContributionResponse).toList();
    }

    @PatchMapping("/{id}/open")
    @PreAuthorize("hasRole('PRESIDENT')")
    TontineDtos.TontineCycleResponse open(@PathVariable UUID id) {
        return toResponse(tontineCycleService.open(id));
    }

    @PatchMapping("/{id}/change-beneficiary")
    @PreAuthorize("hasRole('PRESIDENT')")
    TontineDtos.TontineCycleResponse changeBeneficiary(@PathVariable UUID id, @Valid @RequestBody TontineDtos.ChangeBeneficiaryRequest request) {
        return toResponse(tontineCycleService.changeBeneficiary(id, request.beneficiaryId(), request.reason()));
    }

    @PatchMapping("/{id}/close")
    @PreAuthorize("hasRole('PRESIDENT')")
    TontineDtos.TontineCycleResponse close(@PathVariable UUID id) {
        return toResponse(tontineCycleService.close(id));
    }

    private TontineDtos.TontineCycleResponse toResponse(TontineCycleEntity cycle) {
        return new TontineDtos.TontineCycleResponse(
                cycle.getId(), cycle.getMonth(), cycle.getYear(), cycle.getBeneficiaryId(), cycle.getStatus(),
                cycle.getExpectedAmount(), cycle.getCollectedAmount(), cycle.getTransferredAmount(), cycle.getRemainingAmount(),
                cycle.getOpenedAt(), cycle.getClosedAt(), cycle.getComment()
        );
    }

    private ContributionDtos.ContributionResponse toContributionResponse(ContributionEntity contribution) {
        long remaining = Math.max(0, contribution.getExpectedAmount() - contribution.getPaidAmount());
        return new ContributionDtos.ContributionResponse(
                contribution.getId(), contribution.getCycleId(), contribution.getMemberId(), contribution.getExpectedAmount(),
                contribution.getPaidAmount(), remaining, contribution.getCurrency(), contribution.getStatus(),
                contribution.getPaidAt(), contribution.getValidatedBy(), contribution.getValidatedAt()
        );
    }
}
