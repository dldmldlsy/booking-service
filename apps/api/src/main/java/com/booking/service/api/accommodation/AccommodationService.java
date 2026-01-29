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

import org.springframework.stereotype.Service;

/**
 * 인메모리 숙소/객실/재고 데이터를 관리하는 서비스.
 * 실제 DB 연동 없이 샘플 데이터를 보유하고 예약 시 재고를 차감한다.
 */
@Service
public class AccommodationService {

    private final Map<Long, Accommodation> accommodations = new HashMap<>();
    private final Map<Long, Room> rooms = new HashMap<>();
    private final Map<Long, List<Availability>> availabilityByRoom = new HashMap<>();

    public AccommodationService() {
        seedSampleData();
    }

    public List<Accommodation> getAccommodations() {
        return new ArrayList<>(accommodations.values());
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
}
