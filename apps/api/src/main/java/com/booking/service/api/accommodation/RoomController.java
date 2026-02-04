package com.booking.service.api.accommodation;

import com.booking.service.api.common.ApiResponse;
import java.time.LocalDate;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 객실 단위 조회용 API.
 */
@RestController
@RequestMapping("/rooms")
public class RoomController {

    private final AccommodationService accommodationService;

    public RoomController(AccommodationService accommodationService) {
        this.accommodationService = accommodationService;
    }

    /**
     * 날짜 범위를 필터링한 객실 재고 조회.
     * from 이상, to 미만 구간으로 반환한다.
     */
    @GetMapping("/{roomId}/availability")
    public ApiResponse<Map<LocalDate, Integer>> availability(@PathVariable Long roomId,
                                                             @RequestParam LocalDate from,
                                                             @RequestParam LocalDate to) {
        return ApiResponse.ok(accommodationService.getAvailability(roomId, from, to));
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }
}
