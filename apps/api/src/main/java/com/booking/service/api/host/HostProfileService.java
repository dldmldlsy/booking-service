package com.booking.service.api.host;

import com.booking.service.domain.host.HostProfile;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인메모리 호스트 프로필 서비스.
 */
@Service
public class HostProfileService {

    private final HostProfileRepository repository;

    public HostProfileService(HostProfileRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public HostProfile createOrUpdate(HostProfile profile) {
        return repository.save(profile);
    }

    public Optional<HostProfile> findByMemberId(Long memberId) {
        return repository.findByMember_Id(memberId);
    }
}
