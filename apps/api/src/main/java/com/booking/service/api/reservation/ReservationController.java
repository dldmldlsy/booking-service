package com.booking.service.api.reservation;

import com.booking.service.domain.reservation.Reservation;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
    @ResponseStatus(HttpStatus.CREATED)
    public Reservation create(@RequestBody ReservationService.CreateReservationRequest request) {
        return reservationService.create(request);
    }

    @GetMapping
    public List<Reservation> list() {
        return reservationService.findAll();
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequest(Exception ex) {
        return new ErrorResponse(ex.getMessage());
    }

    public record ErrorResponse(String message) {
    }
}
