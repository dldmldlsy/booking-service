package com.booking.service.api.auth;

import com.booking.service.domain.token.BlacklistedToken;
import com.booking.service.domain.token.RefreshToken;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 토큰 영속화/블랙리스트 관리.
 */
@Service
public class TokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    public TokenService(RefreshTokenRepository refreshTokenRepository,
                        BlacklistedTokenRepository blacklistedTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.blacklistedTokenRepository = blacklistedTokenRepository;
    }

    @Transactional
    public void saveRefreshToken(String token, Long memberId, Instant expiresAt) {
        refreshTokenRepository.save(new RefreshToken(token, memberId, expiresAt));
    }

    @Transactional(readOnly = true)
    public Optional<RefreshToken> findRefreshToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public void deleteRefreshToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }

    @Transactional
    public void blacklist(String token, Instant expiresAt) {
        blacklistedTokenRepository.save(new BlacklistedToken(token, expiresAt));
    }

    @Transactional(readOnly = true)
    public boolean isBlacklisted(String token) {
        return blacklistedTokenRepository.findByToken(token)
                .map(entry -> entry.getExpiresAt().isAfter(Instant.now()))
                .orElse(false);
    }

    @Transactional
    public void cleanupExpired(Instant now) {
        refreshTokenRepository.deleteByExpiresAtBefore(now);
        blacklistedTokenRepository.deleteByExpiresAtBefore(now);
    }
}
