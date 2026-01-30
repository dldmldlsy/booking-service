package com.booking.service.api.accommodation;

import com.booking.service.domain.accommodation.Accommodation;
import com.booking.service.domain.accommodation.Availability;
import com.booking.service.domain.accommodation.Room;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.booking.service.api.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 숙소/객실/재고를 조회하는 최소 API.
 */
@RestController
@RequestMapping("/accommodations")
public class AccommodationController {

    private final AccommodationService accommodationService;

    public AccommodationController(AccommodationService accommodationService) {
        this.accommodationService = accommodationService;
    }

    @GetMapping
    public ApiResponse<List<AccommodationResponse>> list() {
        Map<Long, List<Availability>> availabilityByRoom = accommodationService.getAvailabilityByRoom();
        List<AccommodationResponse> data = accommodationService.getAccommodations().stream()
                .map(acc -> toResponse(acc,
                        accommodationService.getRoomsByAccommodation(acc.getId()),
                        availabilityByRoom))
                .toList();
        return ApiResponse.ok(data);
    }

    private AccommodationResponse toResponse(Accommodation acc, List<Room> rooms, Map<Long, List<Availability>> availabilityByRoom) {
        List<RoomResponse> roomResponses = rooms.stream()
                .map(room -> new RoomResponse(
                        room.getId(),
                        room.getName(),
                        room.getCapacity(),
                        room.getBasePrice().longValue(),
                        availabilityByRoom.getOrDefault(room.getId(), List.of()).stream()
                                .collect(Collectors.toMap(Availability::getDate, Availability::getAvailableCount))
                ))
                .toList();
        return new AccommodationResponse(acc.getId(), acc.getName(), acc.getAddress(), acc.getDescription(), roomResponses);
    }

    public record AccommodationResponse(Long id, String name, String address, String description, List<RoomResponse> rooms) {
    }

    public record RoomResponse(Long id, String name, int capacity, long basePrice,
                               Map<LocalDate, Integer> availabilityByDate) {
    }
}
