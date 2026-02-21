package com.booking.service.api.reservation;

import com.booking.service.api.common.ApiResponse;
import com.booking.service.domain.reservation.Reservation;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

/**
 * 예약 생성/조회에 대한 최소 API.
 */
@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Reservation>> create(@Valid @RequestBody ReservationService.CreateReservationRequest request) {
        Reservation created = reservationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(created));
    }

    @GetMapping("/{id}")
    public ApiResponse<Reservation> getById(@PathVariable Long id) {
        return ApiResponse.ok(reservationService.findById(id));
    }

    @GetMapping
    public ApiResponse<List<Reservation>> list() {
        return ApiResponse.ok(reservationService.findAll());
    }

    @GetMapping("/me")
    public ApiResponse<List<Reservation>> myReservations(Authentication authentication) {
        var member = (com.booking.service.domain.member.Member) authentication.getPrincipal();
        return ApiResponse.ok(reservationService.findByMember(member.getId()));
    }

    @PatchMapping("/{id}/cancel")
    public ApiResponse<Reservation> cancel(@PathVariable Long id) {
        return ApiResponse.ok(reservationService.cancel(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        reservationService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

}
