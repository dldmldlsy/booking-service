package com.booking.service.api.reservation;

import com.booking.service.api.accommodation.AccommodationService;
import com.booking.service.api.accommodation.RoomRepository;
import com.booking.service.api.member.MemberService;
import com.booking.service.api.support.DistributedLockService;
import com.booking.service.api.support.RedisKeys;
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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
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
    private final StringRedisTemplate redis;

    public ReservationService(AccommodationService accommodationService,
                              RoomRepository roomRepository,
                              MemberService memberService,
                              ReservationRepository reservationRepository,
                              DistributedLockService lockService,
                              StringRedisTemplate redis) {
        this.accommodationService = accommodationService;
        this.roomRepository = roomRepository;
        this.memberService = memberService;
        this.reservationRepository = reservationRepository;
        this.lockService = lockService;
        this.redis = redis;
    }

    @Transactional
    @CacheEvict(cacheNames = RedisKeys.CACHE_POPULAR_ACCOMMODATION, allEntries = true)
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
        Reservation saved = reservationRepository.save(reservation);
        incrementPopularityScore(room.getAccommodation().getId(), 1);
        return saved;
    }

    public Reservation findById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("reservation not found: " + id));
    }

    @Transactional
    @CacheEvict(cacheNames = RedisKeys.CACHE_POPULAR_ACCOMMODATION, allEntries = true)
    public Reservation cancel(Long id) {
        Reservation existing = findById(id);
        if (existing.getStatus() == ReservationStatus.CANCELED) {
            throw new IllegalStateException("reservation already canceled");
        }
        accommodationService.releaseAvailability(existing.getRoom().getId(), existing.getCheckInDate(), existing.getCheckOutDate());
        existing.cancel();
        Reservation saved = reservationRepository.save(existing);
        incrementPopularityScore(existing.getRoom().getAccommodation().getId(), -1);
        return saved;
    }

    @Transactional
    @CacheEvict(cacheNames = RedisKeys.CACHE_POPULAR_ACCOMMODATION, allEntries = true)
    public void delete(Long id) {
        Reservation existing = findById(id);
        if (existing.getStatus() == ReservationStatus.RESERVED) {
            accommodationService.releaseAvailability(existing.getRoom().getId(), existing.getCheckInDate(), existing.getCheckOutDate());
        }
        reservationRepository.delete(existing);
        redis.opsForZSet().remove(RedisKeys.HOT_ACCOMMODATION, existing.getRoom().getAccommodation().getId().toString());
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

    private void incrementPopularityScore(Long accommodationId, double delta) {
        ZSetOperations<String, String> zset = redis.opsForZSet();
        zset.incrementScore(RedisKeys.HOT_ACCOMMODATION, accommodationId.toString(), delta);
    }

    public record CreateReservationRequest(
            @NotNull Long memberId,
            @NotNull Long roomId,
            @FutureOrPresent LocalDate checkInDate,
            @Future LocalDate checkOutDate) {
    }
}
