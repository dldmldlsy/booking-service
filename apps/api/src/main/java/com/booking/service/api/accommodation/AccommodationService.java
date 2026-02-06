package com.booking.service.api.accommodation;

import com.booking.service.domain.accommodation.Accommodation;
import com.booking.service.domain.accommodation.Availability;
import com.booking.service.domain.accommodation.Room;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 숙소/객실/재고 데이터를 JPA로 관리하는 서비스.
 */
@Service
public class AccommodationService {

    private final AccommodationRepository accommodationRepository;
    private final RoomRepository roomRepository;
    private final AvailabilityRepository availabilityRepository;

    public AccommodationService(AccommodationRepository accommodationRepository,
                                RoomRepository roomRepository,
                                AvailabilityRepository availabilityRepository) {
        this.accommodationRepository = accommodationRepository;
        this.roomRepository = roomRepository;
        this.availabilityRepository = availabilityRepository;
    }

    @PostConstruct
    @Transactional
    void seedIfEmpty() {
        if (accommodationRepository.count() > 0) return;
        Accommodation seoulStay = accommodationRepository.save(new Accommodation("Seoul Stay", "Seoul", "도심 접근성 좋은 숙소"));
        Accommodation busanBay = accommodationRepository.save(new Accommodation("Busan Bay", "Busan", "해변 근처 숙소"));

        Room seoulTwin = roomRepository.save(new Room(seoulStay, "Twin Room", 2, new BigDecimal("90000")));
        Room seoulSuite = roomRepository.save(new Room(seoulStay, "Suite", 4, new BigDecimal("150000")));
        Room busanOcean = roomRepository.save(new Room(busanBay, "Ocean View", 3, new BigDecimal("120000")));

        LocalDate today = LocalDate.now();
        for (int i = 0; i < 5; i++) {
            LocalDate date = today.plusDays(i);
            availabilityRepository.save(new Availability(seoulTwin, date, 3));
            availabilityRepository.save(new Availability(seoulSuite, date, 2));
            availabilityRepository.save(new Availability(busanOcean, date, 5));
        }
    }

    public List<Accommodation> getAccommodations() {
        return accommodationRepository.findAll();
    }

    public Accommodation getAccommodation(Long accommodationId) {
        return accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new IllegalArgumentException("accommodation not found: " + accommodationId));
    }

    public List<Room> getRoomsByAccommodation(Long accommodationId) {
        return roomRepository.findByAccommodation_Id(accommodationId);
    }

    public Map<Long, List<Availability>> getAvailabilityByRoom() {
        return roomRepository.findAll().stream()
                .collect(Collectors.toMap(Room::getId, room -> availabilityRepository.findByRoom(room)));
    }

    public Optional<Room> findRoom(Long roomId) {
        return roomRepository.findById(roomId);
    }

    @Transactional
    public Accommodation createAccommodation(@Valid CreateAccommodationRequest request) {
        Objects.requireNonNull(request, "request is required");
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        if (request.address() == null || request.address().isBlank()) {
            throw new IllegalArgumentException("address is required");
        }
        Accommodation accommodation = accommodationRepository.save(new Accommodation(request.name(), request.address(), request.description()));

        if (request.rooms() != null) {
            request.rooms().forEach(roomRequest -> createRoom(accommodation, roomRequest));
        }
        return accommodation;
    }

    @Transactional
    public void deleteAccommodation(Long accommodationId) {
        Accommodation existing = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new IllegalArgumentException("accommodation not found: " + accommodationId));
        roomRepository.findByAccommodation_Id(accommodationId)
                .forEach(room -> {
                    availabilityRepository.findByRoom(room).forEach(availabilityRepository::delete);
                    roomRepository.delete(room);
                });
        accommodationRepository.delete(existing);
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
    @Transactional
    public void consumeAvailability(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        iterateDates(checkIn, checkOut)
                .forEach(date -> availabilityFor(roomId, date)
                        .ifPresent(av -> {
                            av.decrement();
                            availabilityRepository.save(av);
                        }));
    }

    /**
     * 예약 취소 시 재고를 복원한다.
     */
    @Transactional
    public void releaseAvailability(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        iterateDates(checkIn, checkOut)
                .forEach(date -> availabilityFor(roomId, date)
                        .ifPresent(av -> {
                            av.increment();
                            availabilityRepository.save(av);
                        }));
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
        Room room = findRoom(roomId).orElseThrow(() -> new IllegalArgumentException("room not found: " + roomId));

        return availabilityRepository.findByRoom(room).stream()
                .filter(av -> !av.getDate().isBefore(from) && av.getDate().isBefore(to))
                .collect(Collectors.toMap(Availability::getDate, Availability::getAvailableCount));
    }

    private Optional<Availability> availabilityFor(Long roomId, LocalDate date) {
        return roomRepository.findById(roomId)
                .flatMap(room -> availabilityRepository.findByRoomAndDate(room, date));
    }

    private Room createRoom(Accommodation accommodation, CreateRoomRequest request) {
        Room room = new Room(accommodation, request.name(), request.capacity(), request.basePrice());
        room = roomRepository.save(room);
        if (request.availabilityByDate() != null) {
            request.availabilityByDate().forEach((date, count) ->
                    availabilityRepository.save(new Availability(room, date, count)));
        }
        return room;
    }

    private java.util.stream.Stream<LocalDate> iterateDates(LocalDate start, LocalDate endExclusive) {
        Objects.requireNonNull(start, "start date");
        Objects.requireNonNull(endExclusive, "end date");
        if (!start.isBefore(endExclusive)) {
            throw new IllegalArgumentException("start must be before end");
        }
        long days = java.time.temporal.ChronoUnit.DAYS.between(start, endExclusive);
        return java.util.stream.LongStream.range(0, days).mapToObj(start::plusDays);
    }

    public record CreateAccommodationRequest(
            @NotBlank String name,
            @NotBlank String address,
            String description,
            List<CreateRoomRequest> rooms) {
    }

    public record CreateRoomRequest(
            @NotBlank String name,
            @Positive int capacity,
            @PositiveOrZero BigDecimal basePrice,
            Map<LocalDate, Integer> availabilityByDate) {
    }
}
