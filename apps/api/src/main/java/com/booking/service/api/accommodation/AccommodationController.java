package com.booking.service.api.accommodation;

import com.booking.service.domain.accommodation.Accommodation;
import com.booking.service.domain.accommodation.Availability;
import com.booking.service.domain.accommodation.Room;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.booking.service.api.common.ApiResponse;
import com.booking.service.api.host.HostProfileService;
import com.booking.service.domain.host.HostProfile;
import com.booking.service.domain.member.Member;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

/**
 * 숙소/객실/재고를 조회하는 최소 API.
 */
@RestController
@RequestMapping("/accommodations")
public class AccommodationController {

    private final AccommodationService accommodationService;
    private final HostProfileService hostProfileService;

    public AccommodationController(AccommodationService accommodationService,
                                   HostProfileService hostProfileService) {
        this.accommodationService = accommodationService;
        this.hostProfileService = hostProfileService;
    }

    @GetMapping("/{id}")
    public ApiResponse<AccommodationResponse> getById(@PathVariable Long id) {
        Map<Long, List<Availability>> availabilityByRoom = accommodationService.getAvailabilityByRoom();
        var acc = accommodationService.getAccommodation(id);
        return ApiResponse.ok(
                toResponse(acc,
                        accommodationService.getRoomsByAccommodation(acc.getId()),
                        availabilityByRoom)
        );
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

    @PostMapping
    public ResponseEntity<ApiResponse<AccommodationResponse>> create(@Valid @RequestBody AccommodationService.CreateAccommodationRequest request,
                                                                     Authentication authentication) {
        Member host = (Member) authentication.getPrincipal();
        HostProfile profile = hostProfileService.findByMemberId(host.getId())
                .orElseThrow(() -> new IllegalStateException("host profile not found"));
        if (profile.getStatus() != HostProfile.Status.APPROVED) {
            throw new IllegalStateException("host profile is not approved");
        }

        var created = accommodationService.createAccommodation(request);
        Map<Long, List<Availability>> availabilityByRoom = accommodationService.getAvailabilityByRoom();
        AccommodationResponse response = toResponse(created,
                accommodationService.getRoomsByAccommodation(created.getId()),
                availabilityByRoom);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @DeleteMapping("/{accommodationId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long accommodationId) {
        accommodationService.deleteAccommodation(accommodationId);
        return ResponseEntity.ok(ApiResponse.ok(null));
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
