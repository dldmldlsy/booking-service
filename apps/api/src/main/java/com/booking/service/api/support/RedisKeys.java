package com.booking.service.api.support;

/**
 * Redis 키 네이밍 컨벤션 모음.
 * 기능별 prefix를 한곳에서 관리해 분산락/토큰/캐시 사용 시 일관성을 유지한다.
 */
public final class RedisKeys {
    private RedisKeys() {}

    public static final String TOKEN_BLACKLIST = "token:blacklist"; // set or hash
    public static final String TOKEN_REFRESH = "token:refresh";     // map: memberId -> refresh token
    public static final String LOCK_PREFIX = "lock:";               // lock:{resource}
    public static final String CACHE_ACCOMMODATION = "cache:accommodation"; // sample cache name
}
