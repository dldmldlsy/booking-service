package com.booking.service.api.host;

import com.booking.service.domain.host.HostProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HostProfileRepository extends JpaRepository<HostProfile, Long> {
    Optional<HostProfile> findByMember_Id(Long memberId);
}
