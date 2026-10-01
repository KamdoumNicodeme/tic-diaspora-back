package com.ticdiaspora.domain.service;

import com.ticdiaspora.domain.model.*;

import com.ticdiaspora.domain.model.enums.ContributionStatus;
import com.ticdiaspora.domain.exception.ForbiddenOperationException;

import java.util.Collection;

public class TontineRules {

    public long expectedAmount(Collection<Long> activeMemberContributionAmounts) {
        return activeMemberContributionAmounts.stream().mapToLong(Long::longValue).sum();
    }

    public ContributionStatus contributionStatus(long expectedAmount, long paidAmount) {
        if (paidAmount <= 0) {
            return ContributionStatus.PENDING;
        }
        if (paidAmount < expectedAmount) {
            return ContributionStatus.PARTIALLY_PAID;
        }
        return ContributionStatus.PAID;
    }

    public long remainingAmount(long expectedAmount, long paidAmount) {
        return Math.max(0, expectedAmount - paidAmount);
    }

    public void verifyClosable(long missingContributions, boolean beneficiaryFullyConfirmed) {
        if (missingContributions > 0) {
            throw new ForbiddenOperationException("Le cycle ne peut pas être clôturé: des cotisations sont manquantes");
        }
        if (!beneficiaryFullyConfirmed) {
            throw new ForbiddenOperationException("Le cycle ne peut pas être clôturé: le bénéficiaire n'a pas confirmé la réception totale");
        }
    }
}
