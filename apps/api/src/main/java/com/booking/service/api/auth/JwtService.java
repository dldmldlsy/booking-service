package com.booking.service.api.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * JWT 발급/검증을 담당한다. Spring Security 없이 단순 유효성 체크만 수행한다.
 */
@Service
public class JwtService {

    private final Key signingKey;
    private final long accessValidityMs;
    private final long refreshValidityMs;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity-ms:900000}") long accessValidityMs,
            @Value("${jwt.refresh-token-validity-ms:604800000}") long refreshValidityMs) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessValidityMs = accessValidityMs;
        this.refreshValidityMs = refreshValidityMs;
    }

    public String generateAccessToken(Long memberId) {
        return buildToken(memberId, accessValidityMs);
    }

    public String generateRefreshToken(Long memberId) {
        return buildToken(memberId, refreshValidityMs);
    }

    public Long parseMemberId(String token) {
        var claims = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return Long.valueOf(claims.getSubject());
    }

    private String buildToken(Long memberId, long validityMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityMs);
        return Jwts.builder()
                .setSubject(String.valueOf(memberId))
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }
}
