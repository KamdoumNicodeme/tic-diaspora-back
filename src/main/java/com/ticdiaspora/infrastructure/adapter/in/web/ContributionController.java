package com.ticdiaspora.infrastructure.adapter.in.web;
import com.ticdiaspora.infrastructure.adapter.in.web.mapper.ContributionWebMapper;

import com.ticdiaspora.application.usecase.*;
import com.ticdiaspora.application.port.out.PaymentProofStoragePort;
import com.ticdiaspora.application.port.out.*;
import com.ticdiaspora.infrastructure.adapter.in.web.dto.*;

import com.ticdiaspora.domain.model.Contribution;
import com.ticdiaspora.domain.model.Member;
import com.ticdiaspora.domain.model.enums.PaymentMethod;
import com.ticdiaspora.domain.exception.BusinessException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/contributions")
public class ContributionController {

    private final ContributionService contributionService;
    private final ContributionWebMapper contributionMapper;
    private final PaymentProofStoragePort paymentProofStorage;
    private final MemberRepositoryPort members;

    public ContributionController(ContributionService contributionService, ContributionWebMapper contributionMapper,
                                  PaymentProofStoragePort paymentProofStorage, MemberRepositoryPort members) {
        this.contributionService = contributionService;
        this.contributionMapper = contributionMapper;
        this.paymentProofStorage = paymentProofStorage;
        this.members = members;
    }

    @GetMapping
    @PreAuthorize("hasRole('PRESIDENT')")
    Page<ContributionDtos.ContributionResponse> list(Pageable pageable) {
        return contributionService.list(pageable).map(this::toResponse);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('PRESIDENT') or authentication.token.claims['memberId'] == #request.memberId().toString()")
    ContributionDtos.ContributionResponse recordPayment(@Valid @RequestBody ContributionDtos.ContributionPaymentRequest request) {
        return toResponse(contributionService.recordPayment(request.cycleId(), request.memberId(), request.amount(),
                request.method(), request.transactionReference(), null));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('PRESIDENT') or authentication.token.claims['memberId'] == #memberId.toString()")
    ContributionDtos.ContributionResponse recordPaymentWithProof(
            @RequestParam UUID cycleId,
            @RequestParam UUID memberId,
            @RequestParam long amount,
            @RequestParam PaymentMethod method,
            @RequestParam(required = false) String transactionReference,
            @RequestPart(required = false) MultipartFile proof
    ) {
        String proofUrl = null;
        if (proof != null && !proof.isEmpty()) {
            try {
                proofUrl = paymentProofStorage.store(proof.getOriginalFilename(), proof.getContentType(), proof.getInputStream()).url();
            } catch (IOException exception) {
                throw new BusinessException("PAYMENT_PROOF_UPLOAD_FAILED", "Impossible de lire la preuve de paiement envoyée");
            }
        }
        return toResponse(contributionService.recordPayment(cycleId, memberId, amount, method, transactionReference, proofUrl));
    }

    @GetMapping("/proofs/{fileName}")
    @PreAuthorize("hasRole('PRESIDENT') or @contributionService.canCurrentUserAccessProof(#fileName)")
    ResponseEntity<byte[]> proof(@PathVariable String fileName) {
        PaymentProofStoragePort.PaymentProofFile proof = paymentProofStorage.load(fileName);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(proof.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(proof.storedFilename()).build().toString())
                .body(proof.content());
    }

    @PatchMapping("/{id}/validate")
    @PreAuthorize("hasRole('PRESIDENT')")
    ContributionDtos.ContributionResponse validate(@PathVariable UUID id) {
        return toResponse(contributionService.validate(id));
    }

    private ContributionDtos.ContributionResponse toResponse(Contribution contribution) {
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
