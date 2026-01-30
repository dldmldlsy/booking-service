package com.booking.service.api.auth;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * 간단한 인메모리 리프레시 토큰 저장소.
 * refreshToken -> memberId 매핑을 보관하고 재발급 시 교체한다.
 */
@Component
public class RefreshTokenStore {
    private final Map<String, Long> store = new ConcurrentHashMap<>();

    public void save(String refreshToken, Long memberId) {
        store.put(refreshToken, memberId);
    }

    public Optional<Long> findMemberId(String refreshToken) {
        return Optional.ofNullable(store.get(refreshToken));
    }

    public void delete(String refreshToken) {
        store.remove(refreshToken);
    }

    public void replace(String oldToken, String newToken, Long memberId) {
        delete(oldToken);
        save(newToken, memberId);
    }
}
