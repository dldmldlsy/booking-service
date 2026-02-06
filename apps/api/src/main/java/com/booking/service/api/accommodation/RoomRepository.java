package com.booking.service.api.accommodation;

import com.booking.service.domain.accommodation.Room;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByAccommodation_Id(Long accommodationId);
}
