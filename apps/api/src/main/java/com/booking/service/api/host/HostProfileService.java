package com.booking.service.api.host;

import com.booking.service.domain.host.HostProfile;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/**
 * 인메모리 호스트 프로필 서비스.
 */
@Service
public class HostProfileService {

    private final Map<Long, HostProfile> profiles = new ConcurrentHashMap<>();

    public HostProfile createOrUpdate(Long memberId, String businessNumber, HostProfile.Status initialStatus) {
        HostProfile profile = new HostProfile(memberId, businessNumber, initialStatus);
        profiles.put(memberId, profile);
        return profile;
    }

    public Optional<HostProfile> findByMemberId(Long memberId) {
        return Optional.ofNullable(profiles.get(memberId));
    }
}
