package com.booking.service.api.auth;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * 인메모리 블랙리스트 저장소. 토큰과 만료시각을 보관해 검증 시 차단한다.
 */
@Component
public class BlacklistStore {

    private final Map<String, Instant> blacklist = new ConcurrentHashMap<>();

    public void blacklist(String token, Instant expiresAt) {
        blacklist.put(token, expiresAt);
    }

    public boolean isBlacklisted(String token) {
        Instant exp = blacklist.get(token);
        if (exp == null) {
            return false;
        }
        // 만료가 지났으면 자동 제거
        if (exp.isBefore(Instant.now())) {
            blacklist.remove(token);
            return false;
        }
        return true;
    }

    public void cleanupExpired(Instant now) {
        blacklist.entrySet().removeIf(e -> e.getValue().isBefore(now));
    }
}
