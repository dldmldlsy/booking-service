package com.booking.service.api.auth;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * 간단한 인메모리 리프레시 토큰 저장소.
 * refreshToken -> TokenMeta(memberId, expiresAt) 매핑을 보관하고 재발급 시 교체한다.
 */
@Component
public class RefreshTokenStore {
    private final Map<String, TokenMeta> store = new ConcurrentHashMap<>();

    public void save(String refreshToken, Long memberId, Instant expiresAt) {
        store.put(refreshToken, new TokenMeta(memberId, expiresAt));
    }

    public Optional<TokenMeta> find(String refreshToken) {
        return Optional.ofNullable(store.get(refreshToken));
    }

    public void delete(String refreshToken) {
        store.remove(refreshToken);
    }

    public void replace(String oldToken, String newToken, Long memberId, Instant expiresAt) {
        delete(oldToken);
        save(newToken, memberId, expiresAt);
    }

    public record TokenMeta(Long memberId, Instant expiresAt) {}

    public void cleanupExpired(Instant now) {
        store.entrySet().removeIf(e -> e.getValue().expiresAt().isBefore(now));
    }
}
