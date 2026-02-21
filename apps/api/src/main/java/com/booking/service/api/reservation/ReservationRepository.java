package com.booking.service.api.reservation;

import com.booking.service.domain.reservation.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByMember_Id(Long memberId);
}
