package com.booking.service.api.reservation;

import com.booking.service.api.accommodation.AccommodationService;
import com.booking.service.api.accommodation.RoomRepository;
import com.booking.service.api.member.MemberService;
import com.booking.service.api.support.DistributedLockService;
import com.booking.service.domain.accommodation.Room;
import com.booking.service.domain.member.Member;
import com.booking.service.domain.reservation.Reservation;
import com.booking.service.domain.reservation.ReservationStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 예약 서비스 (JPA).
 */
@Service
public class ReservationService {

    private final AccommodationService accommodationService;
    private final RoomRepository roomRepository;
    private final MemberService memberService;
    private final ReservationRepository reservationRepository;
    private final DistributedLockService lockService;

    public ReservationService(AccommodationService accommodationService,
                              RoomRepository roomRepository,
                              MemberService memberService,
                              ReservationRepository reservationRepository,
                              DistributedLockService lockService) {
        this.accommodationService = accommodationService;
        this.roomRepository = roomRepository;
        this.memberService = memberService;
        this.reservationRepository = reservationRepository;
        this.lockService = lockService;
    }

    @Transactional
    public Reservation create(CreateReservationRequest request) {
        String lockKey = buildLockKey(request.roomId(), request.checkInDate(), request.checkOutDate());
        return lockService.executeWithLock(lockKey, () -> createInternal(request));
    }

    private Reservation createInternal(CreateReservationRequest request) {
        validateDateRange(request.checkInDate(), request.checkOutDate());
        Room room = roomRepository.findById(request.roomId())
                .orElseThrow(() -> new IllegalArgumentException("room not found: " + request.roomId()));
        Member member = memberService.findById(request.memberId())
                .orElseThrow(() -> new IllegalArgumentException("member not found: " + request.memberId()));

        if (!accommodationService.hasAvailability(room.getId(), request.checkInDate(), request.checkOutDate())) {
            throw new IllegalStateException("not enough availability for requested dates");
        }

        accommodationService.consumeAvailability(room.getId(), request.checkInDate(), request.checkOutDate());

        Reservation reservation = new Reservation(
                member,
                room,
                request.checkInDate(),
                request.checkOutDate(),
                ReservationStatus.RESERVED,
                LocalDateTime.now()
        );
        return reservationRepository.save(reservation);
    }

    public Reservation findById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("reservation not found: " + id));
    }

    @Transactional
    public Reservation cancel(Long id) {
        Reservation existing = findById(id);
        if (existing.getStatus() == ReservationStatus.CANCELED) {
            throw new IllegalStateException("reservation already canceled");
        }
        accommodationService.releaseAvailability(existing.getRoom().getId(), existing.getCheckInDate(), existing.getCheckOutDate());
        existing.cancel();
        return reservationRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Reservation existing = findById(id);
        if (existing.getStatus() == ReservationStatus.RESERVED) {
            accommodationService.releaseAvailability(existing.getRoom().getId(), existing.getCheckInDate(), existing.getCheckOutDate());
        }
        reservationRepository.delete(existing);
    }

    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    private String buildLockKey(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        return "lock:reservation:" + roomId + ":" + checkIn + ":" + checkOut;
    }

    private void validateDateRange(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new IllegalArgumentException("check-in/out date is required");
        }
        if (!checkIn.isBefore(checkOut)) {
            throw new IllegalArgumentException("checkInDate must be before checkOutDate");
        }
    }

    public record CreateReservationRequest(
            @NotNull Long memberId,
            @NotNull Long roomId,
            @FutureOrPresent LocalDate checkInDate,
            @Future LocalDate checkOutDate) {
    }
}
