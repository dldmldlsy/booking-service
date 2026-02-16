package com.booking.service.api.auth;

import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * 토큰 영속화/블랙리스트 관리.
 */
@Service
public class TokenService {

    private final RedisTokenRepository redisTokenRepository;

    public TokenService(RedisTokenRepository redisTokenRepository) {
        this.redisTokenRepository = redisTokenRepository;
    }

    public void saveRefreshToken(String token, Long memberId, Instant expiresAt) {
        redisTokenRepository.saveRefreshToken(token, memberId, expiresAt);
    }

    public Optional<RefreshTokenInfo> findRefreshToken(String token) {
        return redisTokenRepository.findRefreshToken(token);
    }

    public void deleteRefreshToken(String token) {
        redisTokenRepository.deleteRefreshToken(token);
    }

    public void blacklist(String token, Instant expiresAt) {
        redisTokenRepository.blacklist(token, expiresAt);
    }

    public boolean isBlacklisted(String token) {
        return redisTokenRepository.isBlacklisted(token);
    }
}
