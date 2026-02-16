package com.booking.service.api.auth;

import com.booking.service.api.support.RedisKeys;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

/**
 * Redis 기반 토큰 저장/블랙리스트 저장소.
 * TTL을 키에 직접 걸어 만료 시 자동 삭제된다.
 */
@Repository
public class RedisTokenRepository {

    private final StringRedisTemplate redis;

    public RedisTokenRepository(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void saveRefreshToken(String token, Long memberId, Instant expiresAt) {
        Duration ttl = Duration.between(Instant.now(), expiresAt);
        if (ttl.isNegative() || ttl.isZero()) {
            return;
        }
        String key = refreshKey(token);
        redis.opsForValue().set(key, memberId.toString(), ttl);
    }

    public Optional<RefreshTokenInfo> findRefreshToken(String token) {
        String key = refreshKey(token);
        String memberId = redis.opsForValue().get(key);
        if (memberId == null) {
            return Optional.empty();
        }
        Long ttlSeconds = redis.getExpire(key);
        Instant expiresAt = ttlSeconds != null && ttlSeconds > 0
                ? Instant.now().plusSeconds(ttlSeconds)
                : Instant.now();
        return Optional.of(new RefreshTokenInfo(token, Long.valueOf(memberId), expiresAt));
    }

    public void deleteRefreshToken(String token) {
        redis.delete(refreshKey(token));
    }

    public void blacklist(String token, Instant expiresAt) {
        Duration ttl = Duration.between(Instant.now(), expiresAt);
        if (ttl.isNegative() || ttl.isZero()) {
            return;
        }
        redis.opsForValue().set(blacklistKey(token), "1", ttl);
    }

    public boolean isBlacklisted(String token) {
        Boolean exists = redis.hasKey(blacklistKey(token));
        return Boolean.TRUE.equals(exists);
    }

    private String refreshKey(String token) {
        return RedisKeys.TOKEN_REFRESH + ":" + token;
    }

    private String blacklistKey(String token) {
        return RedisKeys.TOKEN_BLACKLIST + ":" + token;
    }
}
