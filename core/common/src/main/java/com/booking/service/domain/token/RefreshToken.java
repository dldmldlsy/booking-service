package com.booking.service.domain.token;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * 발급된 리프레시 토큰을 영속화해 재발급 및 블랙리스트 검증에 사용한다.
 */
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 512)
    private String token;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Instant expiresAt;

    protected RefreshToken() {
    }

    public RefreshToken(String token, Long memberId, Instant expiresAt) {
        this.token = token;
        this.memberId = memberId;
        this.expiresAt = expiresAt;
    }

    public Long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public Long getMemberId() {
        return memberId;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
