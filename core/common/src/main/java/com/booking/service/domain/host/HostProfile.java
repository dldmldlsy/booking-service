package com.booking.service.domain.host;

import java.util.Objects;

/**
 * 호스트(사업자) 프로필 도메인.
 */
public class HostProfile {
    private final Long memberId;
    private final String businessNumber;
    private final Status status;

    public HostProfile(Long memberId, String businessNumber, Status status) {
        this.memberId = Objects.requireNonNull(memberId, "memberId is required");
        this.businessNumber = Objects.requireNonNull(businessNumber, "businessNumber is required");
        this.status = Objects.requireNonNull(status, "status is required");
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getBusinessNumber() {
        return businessNumber;
    }

    public Status getStatus() {
        return status;
    }

    public HostProfile withStatus(Status newStatus) {
        return new HostProfile(memberId, businessNumber, newStatus);
    }

    public enum Status {
        PENDING,
        APPROVED,
        REJECTED
    }
}
