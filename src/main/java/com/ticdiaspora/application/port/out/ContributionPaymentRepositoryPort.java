package com.ticdiaspora.application.port.out;

import com.ticdiaspora.domain.model.*;
import com.ticdiaspora.domain.model.enums.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.*;
import java.util.*;

public interface ContributionPaymentRepositoryPort {
    ContributionPayment save(ContributionPayment contributionPayment);

    List<ContributionPayment> findAllByContributionId(UUID contributionId);

    Optional<ContributionPayment> findFirstByContributionIdOrderByPaidAtDesc(UUID contributionId);

    Optional<ContributionPayment> findByProofUrl(String proofUrl);
}
