package com.booking.service.api.auth;

import java.time.Instant;

/**
 * 리프레시 토큰 메타 정보 DTO.
 * 도메인 엔티티를 제거한 후 Redis/서비스 간 전달용으로 사용한다.
 */
public record RefreshTokenInfo(String token, Long memberId, Instant expiresAt) {
}
