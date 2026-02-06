package com.booking.service.api.accommodation;

import com.booking.service.domain.accommodation.Availability;
import com.booking.service.domain.accommodation.Room;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
    Optional<Availability> findByRoomAndDate(Room room, LocalDate date);
    List<Availability> findByRoom(Room room);
}
