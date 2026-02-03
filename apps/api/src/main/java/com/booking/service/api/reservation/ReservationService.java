package com.booking.service.api.reservation;

import com.booking.service.api.accommodation.AccommodationService;
import com.booking.service.domain.reservation.Reservation;
import com.booking.service.domain.reservation.ReservationStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Optional;

import org.springframework.stereotype.Service;

/**
 * 인메모리 예약 서비스. 재고 확인 후 예약을 생성하고 보관한다.
 */
@Service
public class ReservationService {

    private final AccommodationService accommodationService;
    private final Map<Long, Reservation> reservations = new LinkedHashMap<>();
    private final AtomicLong seq = new AtomicLong(1);

    public ReservationService(AccommodationService accommodationService) {
        this.accommodationService = accommodationService;
    }

    public Reservation create(CreateReservationRequest request) {
        validateDateRange(request.checkInDate(), request.checkOutDate());
        var room = accommodationService.findRoom(request.roomId())
                .orElseThrow(() -> new IllegalArgumentException("room not found: " + request.roomId()));

        if (!accommodationService.hasAvailability(room.getId(), request.checkInDate(), request.checkOutDate())) {
            throw new IllegalStateException("not enough availability for requested dates");
        }

        accommodationService.consumeAvailability(room.getId(), request.checkInDate(), request.checkOutDate());

        Long id = seq.getAndIncrement();
        Reservation reservation = new Reservation(
                id,
                request.memberId(),
                room.getId(),
                request.checkInDate(),
                request.checkOutDate(),
                ReservationStatus.RESERVED,
                LocalDateTime.now()
        );
        reservations.put(id, reservation);
        return reservation;
    }

    public Reservation findById(Long id) {
        return Optional.ofNullable(reservations.get(id))
                .orElseThrow(() -> new IllegalArgumentException("reservation not found: " + id));
    }

    public Reservation cancel(Long id) {
        Reservation existing = findById(id);
        if (existing.getStatus() == ReservationStatus.CANCELED) {
            throw new IllegalStateException("reservation already canceled");
        }
        accommodationService.releaseAvailability(existing.getRoomId(), existing.getCheckInDate(), existing.getCheckOutDate());
        Reservation canceled = new Reservation(
                existing.getId(),
                existing.getMemberId(),
                existing.getRoomId(),
                existing.getCheckInDate(),
                existing.getCheckOutDate(),
                ReservationStatus.CANCELED,
                existing.getCreatedAt()
        );
        reservations.put(id, canceled);
        return canceled;
    }

    public void delete(Long id) {
        Reservation existing = findById(id);
        if (existing.getStatus() == ReservationStatus.RESERVED) {
            accommodationService.releaseAvailability(existing.getRoomId(), existing.getCheckInDate(), existing.getCheckOutDate());
        }
        reservations.remove(id);
    }

    public List<Reservation> findAll() {
        return new ArrayList<>(reservations.values());
    }

    private void validateDateRange(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new IllegalArgumentException("check-in/out date is required");
        }
        if (!checkIn.isBefore(checkOut)) {
            throw new IllegalArgumentException("checkInDate must be before checkOutDate");
        }
    }

    public record CreateReservationRequest(Long memberId, Long roomId, LocalDate checkInDate, LocalDate checkOutDate) {
    }
}
