package com.ticdiaspora.penalty.infrastructure.web;

import com.ticdiaspora.penalty.infrastructure.persistence.PenaltyEntity;
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
@RequestMapping("/api/penalties")
public class PenaltyController {

    private final PenaltyService penaltyService;

    public PenaltyController(PenaltyService penaltyService) {
        this.penaltyService = penaltyService;
    }

    @GetMapping
    @PreAuthorize("hasRole('PRESIDENT')")
    Page<PenaltyDtos.PenaltyResponse> list(Pageable pageable) {
        return penaltyService.list(pageable).map(this::toResponse);
    }

    @PostMapping
    @PreAuthorize("hasRole('PRESIDENT')")
    PenaltyDtos.PenaltyResponse create(@Valid @RequestBody PenaltyDtos.PenaltyRequest request) {
        return toResponse(penaltyService.create(request.memberId(), request.meetingId(), request.type(), request.amount(), request.reason()));
    }

    @PatchMapping("/{id}/pay")
    @PreAuthorize("hasRole('PRESIDENT')")
    PenaltyDtos.PenaltyResponse pay(@PathVariable UUID id) {
        return toResponse(penaltyService.pay(id));
    }

    @PatchMapping("/{id}/waive")
    @PreAuthorize("hasRole('PRESIDENT')")
    PenaltyDtos.PenaltyResponse waive(@PathVariable UUID id, @Valid @RequestBody PenaltyDtos.WaivePenaltyRequest request) {
        return toResponse(penaltyService.waive(id, request.reason()));
    }

    private PenaltyDtos.PenaltyResponse toResponse(PenaltyEntity penalty) {
        return new PenaltyDtos.PenaltyResponse(
                penalty.getId(), penalty.getMemberId(), penalty.getMeetingId(), penalty.getType(),
                penalty.getAmount(), penalty.getStatus(), penalty.getReason(), penalty.getCreatedAt(),
                penalty.getPaidAt(), penalty.getValidatedBy(), penalty.getCancellationReason()
        );
    }
}
