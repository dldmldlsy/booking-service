package com.booking.service.domain.host;

import com.booking.service.domain.member.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * 호스트(사업자) 프로필 도메인.
 */
@Entity
@Table(name = "host_profiles")
public class HostProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    @Column(nullable = false, length = 50)
    private String businessNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    protected HostProfile() {
    }

    public HostProfile(Member member, String businessNumber, Status status) {
        this.member = member;
        this.businessNumber = businessNumber;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public String getBusinessNumber() {
        return businessNumber;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public enum Status {
        PENDING,
        APPROVED,
        REJECTED
    }
}
