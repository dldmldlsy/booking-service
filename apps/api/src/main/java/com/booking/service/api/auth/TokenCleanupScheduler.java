package com.booking.service.api.auth;

import java.time.Instant;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 만료된 토큰/블랙리스트 항목을 주기적으로 정리한다.
 */
@Component
public class TokenCleanupScheduler {

    private final TokenService tokenService;

    public TokenCleanupScheduler(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Scheduled(fixedDelayString = "${token.cleanup-interval-ms:300000}")
    public void cleanup() {
        tokenService.cleanupExpired(Instant.now());
    }
}
