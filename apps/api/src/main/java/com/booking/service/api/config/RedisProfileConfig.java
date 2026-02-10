package com.booking.service.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;

/**
 * 프로퍼티 기반 Redis 접속 설정.
 * 기본 호스트/포트 프로필을 제공해두고, 필요 시 커스텀 빈으로 재정의 가능.
 */
@Configuration
public class RedisProfileConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String host;

    @Value("${spring.data.redis.port:6379}")
    private int port;

    @ConditionalOnMissingBean
    @Bean
    public RedisStandaloneConfiguration redisStandaloneConfiguration() {
        return new RedisStandaloneConfiguration(host, port);
    }
}
