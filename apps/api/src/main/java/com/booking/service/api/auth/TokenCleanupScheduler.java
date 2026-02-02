package com.booking.service.api.auth;

import java.time.Instant;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 만료된 토큰/블랙리스트 항목을 주기적으로 정리한다.
 */
@Component
public class TokenCleanupScheduler {

    private final RefreshTokenStore refreshTokenStore;
    private final BlacklistStore blacklistStore;

    public TokenCleanupScheduler(RefreshTokenStore refreshTokenStore, BlacklistStore blacklistStore) {
        this.refreshTokenStore = refreshTokenStore;
        this.blacklistStore = blacklistStore;
    }

    @Scheduled(fixedDelayString = "${token.cleanup-interval-ms:300000}")
    public void cleanup() {
        Instant now = Instant.now();
        refreshTokenStore.cleanupExpired(now);
        blacklistStore.cleanupExpired(now);
    }
}
