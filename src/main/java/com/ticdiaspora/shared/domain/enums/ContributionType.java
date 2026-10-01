package com.ticdiaspora.shared.domain.enums;

public enum ContributionType {
    XAF_50000(50_000L),
    XAF_100000(100_000L);

    private final long monthlyAmount;

    ContributionType(long monthlyAmount) {
        this.monthlyAmount = monthlyAmount;
    }

    public long monthlyAmount() {
        return monthlyAmount;
    }
}
