package com.booking.service.api.accommodation;

import com.booking.service.domain.accommodation.Accommodation;
import com.booking.service.domain.accommodation.Availability;
import com.booking.service.domain.accommodation.Room;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * 인메모리 숙소/객실/재고 데이터를 관리하는 서비스.
 * 실제 DB 연동 없이 샘플 데이터를 보유하고 예약 시 재고를 차감한다.
 */
@Service
public class AccommodationService {

    private final Map<Long, Accommodation> accommodations = new HashMap<>();
    private final Map<Long, Room> rooms = new HashMap<>();
    private final Map<Long, List<Availability>> availabilityByRoom = new HashMap<>();
    private final AtomicLong accommodationSeq = new AtomicLong(1);
    private final AtomicLong roomSeq = new AtomicLong(1);
    private final AtomicLong availabilitySeq = new AtomicLong(1);

    public AccommodationService() {
        seedSampleData();
        long nextId = accommodations.keySet().stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L) + 1;
        accommodationSeq.set(nextId);

        long nextRoomId = rooms.keySet().stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L) + 1;
        roomSeq.set(nextRoomId);

        long nextAvailabilityId = availabilityByRoom.values().stream()
                .flatMap(List::stream)
                .mapToLong(Availability::getId)
                .max()
                .orElse(0L) + 1;
        availabilitySeq.set(nextAvailabilityId);
    }

    public List<Accommodation> getAccommodations() {
        return new ArrayList<>(accommodations.values());
    }

    public Accommodation getAccommodation(Long accommodationId) {
        return Optional.ofNullable(accommodations.get(accommodationId))
                .orElseThrow(() -> new IllegalArgumentException("accommodation not found: " + accommodationId));
    }

    public List<Room> getRoomsByAccommodation(Long accommodationId) {
        return rooms.values().stream()
                .filter(r -> r.getAccommodationId().equals(accommodationId))
                .toList();
    }

    public Map<Long, List<Availability>> getAvailabilityByRoom() {
        return Collections.unmodifiableMap(availabilityByRoom);
    }

    public Optional<Room> findRoom(Long roomId) {
        return Optional.ofNullable(rooms.get(roomId));
    }

    public Accommodation createAccommodation(@Valid CreateAccommodationRequest request) {
        Objects.requireNonNull(request, "request is required");
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        if (request.address() == null || request.address().isBlank()) {
            throw new IllegalArgumentException("address is required");
        }
        Long id = accommodationSeq.getAndIncrement();
        Accommodation accommodation = new Accommodation(id, request.name(), request.address(), request.description());
        accommodations.put(id, accommodation);

        if (request.rooms() != null) {
            request.rooms().forEach(roomRequest -> createRoom(id, roomRequest));
        }
        return accommodation;
    }

    public void deleteAccommodation(Long accommodationId) {
        Accommodation removed = accommodations.remove(accommodationId);
        if (removed == null) {
            throw new IllegalArgumentException("accommodation not found: " + accommodationId);
        }
        // Remove rooms and related availability for this accommodation
        rooms.entrySet().removeIf(entry -> {
            if (entry.getValue().getAccommodationId().equals(accommodationId)) {
                availabilityByRoom.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }

    /**
     * 체크인~체크아웃-1 모든 날짜에 재고가 있는지 확인한다.
     */
    public boolean hasAvailability(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        return iterateDates(checkIn, checkOut)
                .allMatch(date -> availabilityFor(roomId, date)
                        .map(av -> av.getAvailableCount() > 0)
                        .orElse(false));
    }

    /**
     * 재고를 차감한다. 사전에 hasAvailability로 검증되어야 한다.
     */
    public void consumeAvailability(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        iterateDates(checkIn, checkOut)
                .forEach(date -> availabilityFor(roomId, date)
                        .ifPresent(Availability::decrement));
    }

    /**
     * 예약 취소 시 재고를 복원한다.
     */
    public void releaseAvailability(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        iterateDates(checkIn, checkOut)
                .forEach(date -> availabilityFor(roomId, date)
                        .ifPresent(Availability::increment));
    }

    /**
     * 지정한 방의 재고를 날짜 범위(from 이상, to 미만)로 필터링하여 반환한다.
     */
    public Map<LocalDate, Integer> getAvailability(Long roomId, LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("from/to date is required");
        }
        if (!from.isBefore(to)) {
            throw new IllegalArgumentException("from must be before to");
        }
        // room 존재 확인
        findRoom(roomId).orElseThrow(() -> new IllegalArgumentException("room not found: " + roomId));

        return availabilityByRoom.getOrDefault(roomId, List.of()).stream()
                .filter(av -> !av.getDate().isBefore(from) && av.getDate().isBefore(to))
                .collect(Collectors.toMap(Availability::getDate, Availability::getAvailableCount));
    }

    private Optional<Availability> availabilityFor(Long roomId, LocalDate date) {
        return availabilityByRoom.getOrDefault(roomId, List.of())
                .stream()
                .filter(av -> av.getDate().equals(date))
                .findFirst();
    }

    private void seedSampleData() {
        Accommodation seoulStay = new Accommodation(1L, "Seoul Stay", "Seoul", "도심 접근성 좋은 숙소");
        Accommodation busanBay = new Accommodation(2L, "Busan Bay", "Busan", "해변 근처 숙소");
        accommodations.put(seoulStay.getId(), seoulStay);
        accommodations.put(busanBay.getId(), busanBay);

        Room seoulTwin = new Room(101L, 1L, "Twin Room", 2, new BigDecimal("90000"));
        Room seoulSuite = new Room(102L, 1L, "Suite", 4, new BigDecimal("150000"));
        Room busanOcean = new Room(201L, 2L, "Ocean View", 3, new BigDecimal("120000"));
        rooms.put(seoulTwin.getId(), seoulTwin);
        rooms.put(seoulSuite.getId(), seoulSuite);
        rooms.put(busanOcean.getId(), busanOcean);

        LocalDate today = LocalDate.now();
        // 간단히 5일치 재고를 채워둔다.
        availabilityByRoom.put(seoulTwin.getId(), new ArrayList<>());
        availabilityByRoom.put(seoulSuite.getId(), new ArrayList<>());
        availabilityByRoom.put(busanOcean.getId(), new ArrayList<>());

        for (int i = 0; i < 5; i++) {
            LocalDate date = today.plusDays(i);
            availabilityByRoom.get(seoulTwin.getId()).add(new Availability(1_000L + i, seoulTwin.getId(), date, 3));
            availabilityByRoom.get(seoulSuite.getId()).add(new Availability(2_000L + i, seoulSuite.getId(), date, 2));
            availabilityByRoom.get(busanOcean.getId()).add(new Availability(3_000L + i, busanOcean.getId(), date, 5));
        }
    }

    private java.util.stream.Stream<LocalDate> iterateDates(LocalDate start, LocalDate endExclusive) {
        Objects.requireNonNull(start, "start date");
        Objects.requireNonNull(endExclusive, "end date");
        if (!start.isBefore(endExclusive)) {
            throw new IllegalArgumentException("start must be before end");
        }
        long days = java.time.temporal.ChronoUnit.DAYS.between(start, endExclusive);
        return java.util.stream.Stream.iterate(start, d -> d.plusDays(1)).limit(days);
    }

    private Room createRoom(Long accommodationId, CreateRoomRequest request) {
        Objects.requireNonNull(request, "room request is required");
        Long roomId = roomSeq.getAndIncrement();
        Room room = new Room(roomId, accommodationId, request.name(), request.capacity(), BigDecimal.valueOf(request.basePrice()));
        rooms.put(roomId, room);

        Map<LocalDate, Integer> availabilityByDate = request.availabilityByDate() == null
                ? Map.of()
                : request.availabilityByDate();
        List<Availability> availabilityList = availabilityByRoom.computeIfAbsent(roomId, k -> new ArrayList<>());
        availabilityByDate.forEach((date, count) -> {
            if (date == null) {
                throw new IllegalArgumentException("availability date is required");
            }
            if (count == null || count < 0) {
                throw new IllegalArgumentException("availability count must be >= 0");
            }
            Long availabilityId = availabilitySeq.getAndIncrement();
            availabilityList.add(new Availability(availabilityId, roomId, date, count));
        });
        return room;
    }

    public record CreateAccommodationRequest(
            @NotBlank String name,
            @NotBlank String address,
            String description,
            @Valid List<CreateRoomRequest> rooms) {
    }

    public record CreateRoomRequest(
            @NotBlank String name,
            @Positive int capacity,
            @PositiveOrZero long basePrice,
            Map<LocalDate, Integer> availabilityByDate) {
    }
}
