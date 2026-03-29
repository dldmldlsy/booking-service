package com.booking.service.api.accommodation;

import com.booking.service.domain.accommodation.Accommodation;
import com.booking.service.domain.accommodation.AccommodationCategory;
import com.booking.service.domain.accommodation.Availability;
import com.booking.service.domain.accommodation.Room;
import com.booking.service.domain.accommodation.RoomType;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.booking.service.api.support.RedisKeys;

/**
 * 숙소/객실/재고 데이터를 JPA로 관리하는 서비스.
 */
@Service
public class AccommodationService {

    private final AccommodationRepository accommodationRepository;
    private final RoomRepository roomRepository;
    private final AvailabilityRepository availabilityRepository;
    private final StringRedisTemplate redis;

    public AccommodationService(AccommodationRepository accommodationRepository,
                                RoomRepository roomRepository,
                                AvailabilityRepository availabilityRepository,
                                StringRedisTemplate redis) {
        this.accommodationRepository = accommodationRepository;
        this.roomRepository = roomRepository;
        this.availabilityRepository = availabilityRepository;
        this.redis = redis;
    }

    @PostConstruct
    @Transactional
    void seedIfEmpty() {
        if (accommodationRepository.count() > 0) return;
        Accommodation seoulStay = accommodationRepository.save(
                new Accommodation(
                        "Seoul Stay",
                        "Seoul",
                        "도심 접근성 좋은 숙소",
                        "010-0000-0000",
                        37.5665,
                        126.9780,
                        "서울",
                        AccommodationCategory.HOTEL,
                        LocalTime.of(15, 0),
                        LocalTime.of(11, 0),
                        200,
                        new BigDecimal("90000"),
                        "https://example.com/seoul.jpg",
                        null,
                        null));
        Accommodation busanBay = accommodationRepository.save(
                new Accommodation(
                        "Busan Bay",
                        "Busan",
                        "해변 근처 숙소",
                        "010-1111-2222",
                        35.1796,
                        129.0756,
                        "부산",
                        AccommodationCategory.PENSION,
                        LocalTime.of(15, 0),
                        LocalTime.of(11, 0),
                        300,
                        new BigDecimal("80000"),
                        "https://example.com/busan.jpg",
                        null,
                        null));

        Room seoulTwin = roomRepository.save(new Room(seoulStay, "Twin Room", 2, new BigDecimal("90000"), RoomType.TWIN,
                "아늑한 트윈룸", 2, LocalTime.of(15, 0), LocalTime.of(11, 0), "https://example.com/seoul-twin.jpg"));
        Room seoulSuite = roomRepository.save(new Room(seoulStay, "Suite", 4, new BigDecimal("150000"), RoomType.SUITE,
                "가족용 스위트", 4, LocalTime.of(15, 0), LocalTime.of(11, 0), "https://example.com/seoul-suite.jpg"));
        Room busanOcean = roomRepository.save(new Room(busanBay, "Ocean View", 3, new BigDecimal("120000"), RoomType.DELUXE,
                "오션뷰 객실", 3, LocalTime.of(15, 0), LocalTime.of(11, 0), "https://example.com/busan-ocean.jpg"));

        LocalDate today = LocalDate.now();
        for (int i = 0; i < 5; i++) {
            LocalDate date = today.plusDays(i);
            availabilityRepository.save(new Availability(seoulTwin, date, 3));
            availabilityRepository.save(new Availability(seoulSuite, date, 2));
            availabilityRepository.save(new Availability(busanOcean, date, 5));
        }
    }

    @Cacheable(cacheNames = RedisKeys.CACHE_ACCOMMODATION, key = "'all'")
    public List<Accommodation> getAccommodations() {
        return accommodationRepository.findAll();
    }

    @Cacheable(cacheNames = RedisKeys.CACHE_ACCOMMODATION, key = "'id:' + #accommodationId")
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
    @CacheEvict(cacheNames = RedisKeys.CACHE_ACCOMMODATION, allEntries = true)
    public Accommodation createAccommodation(@Valid CreateAccommodationRequest request) {
        Objects.requireNonNull(request, "request is required");
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        if (request.address() == null || request.address().isBlank()) {
            throw new IllegalArgumentException("address is required");
        }
        validateAccommodationTimes(request.checkInTime(), request.checkOutTime());
        if (request.maximumCapacity() != null && request.maximumCapacity() <= 0) {
            throw new IllegalArgumentException("maximumCapacity must be positive");
        }
        if (request.lowestPrice() != null && request.lowestPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("lowestPrice must be >= 0");
        }

        Accommodation accommodation = accommodationRepository.save(
                new Accommodation(
                        request.name(),
                        request.address(),
                        request.description(),
                        request.phoneNumber(),
                        request.latitude(),
                        request.longitude(),
                        request.region(),
                        request.category(),
                        request.checkInTime(),
                        request.checkOutTime(),
                        request.maximumCapacity(),
                        request.lowestPrice(),
                        request.thumbnailUrl(),
                        null,
                        null));

        if (request.rooms() != null) {
            request.rooms().forEach(roomRequest -> createRoom(accommodation, roomRequest));
        }
        return accommodation;
    }

    @Transactional
    @CacheEvict(cacheNames = {RedisKeys.CACHE_ACCOMMODATION, RedisKeys.CACHE_POPULAR_ACCOMMODATION}, allEntries = true)
    public void deleteAccommodation(Long accommodationId) {
        Accommodation existing = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new IllegalArgumentException("accommodation not found: " + accommodationId));
        roomRepository.findByAccommodation_Id(accommodationId)
                .forEach(room -> {
                    availabilityRepository.findByRoom(room).forEach(availabilityRepository::delete);
                    roomRepository.delete(room);
                });
        accommodationRepository.delete(existing);
        redis.opsForZSet().remove(RedisKeys.HOT_ACCOMMODATION, accommodationId.toString());
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
        validateRoomRequest(request);
        Room savedRoom = roomRepository.save(new Room(
                accommodation,
                request.name(),
                request.capacity(),
                request.basePrice(),
                request.roomType(),
                request.description(),
                request.maxHeadCount(),
                request.checkInTime(),
                request.checkOutTime(),
                request.thumbnailUrl()));
        if (request.availabilityByDate() != null) {
            request.availabilityByDate().forEach((date, count) ->
                    availabilityRepository.save(new Availability(savedRoom, date, count)));
        }
        return savedRoom;
    }

    /**
     * 인기 숙소 상위 N개를 반환한다. Redis ZSET을 우선 조회하며, 데이터가 없을 경우 DB 리스트를 fallback으로 사용한다.
     */
    @Cacheable(cacheNames = RedisKeys.CACHE_POPULAR_ACCOMMODATION, key = "'v1:' + #limit")
    public List<Accommodation> getPopularAccommodations(int limit) {
        if (limit <= 0) {
            return List.of();
        }
        ZSetOperations<String, String> zset = redis.opsForZSet();
        Set<String> idStrings = zset.reverseRange(RedisKeys.HOT_ACCOMMODATION, 0, limit - 1);
        if (idStrings == null || idStrings.isEmpty()) {
            return accommodationRepository.findAll().stream().limit(limit).toList();
        }

        List<Long> orderedIds = idStrings.stream()
                .map(Long::valueOf)
                .toList();

        Map<Long, Accommodation> byId = accommodationRepository.findAllById(orderedIds).stream()
                .collect(Collectors.toMap(Accommodation::getId, Function.identity()));

        return orderedIds.stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .toList();
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
            String phoneNumber,
            Double latitude,
            Double longitude,
            String region,
            AccommodationCategory category,
            LocalTime checkInTime,
            LocalTime checkOutTime,
            Integer maximumCapacity,
            BigDecimal lowestPrice,
            String thumbnailUrl,
            List<CreateRoomRequest> rooms) {
    }

    public record CreateRoomRequest(
            @NotBlank String name,
            @Positive int capacity,
            @PositiveOrZero BigDecimal basePrice,
            RoomType roomType,
            String description,
            Integer maxHeadCount,
            LocalTime checkInTime,
            LocalTime checkOutTime,
            String thumbnailUrl,
            Map<LocalDate, Integer> availabilityByDate) {
    }

    private void validateAccommodationTimes(LocalTime checkIn, LocalTime checkOut) {
        if (checkIn != null && checkOut != null && !checkIn.isBefore(checkOut)) {
            throw new IllegalArgumentException("checkInTime must be before checkOutTime");
        }
    }

    private void validateRoomRequest(CreateRoomRequest request) {
        if (request.maxHeadCount() != null && request.maxHeadCount() < request.capacity()) {
            throw new IllegalArgumentException("maxHeadCount must be >= capacity");
        }
        if (request.checkInTime() != null && request.checkOutTime() != null &&
                !request.checkInTime().isBefore(request.checkOutTime())) {
            throw new IllegalArgumentException("room checkInTime must be before checkOutTime");
        }
    }
}
