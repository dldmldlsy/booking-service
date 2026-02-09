package com.booking.service.api.auth;

import com.booking.service.domain.token.BlacklistedToken;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long> {
    Optional<BlacklistedToken> findByToken(String token);
    void deleteByExpiresAtBefore(Instant cutoff);
}
