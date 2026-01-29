package com.booking.service.domain.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 예약 도메인 모델. 단순 POJO이며 생성 시 기본 검증을 수행한다.
 */
public class Reservation {
    private final Long id;
    private final Long memberId;
    private final Long roomId;
    private final LocalDate checkInDate;
    private final LocalDate checkOutDate;
    private final ReservationStatus status;
    private final LocalDateTime createdAt;

    public Reservation(Long id,
                       Long memberId,
                       Long roomId,
                       LocalDate checkInDate,
                       LocalDate checkOutDate,
                       ReservationStatus status,
                       LocalDateTime createdAt) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.memberId = Objects.requireNonNull(memberId, "memberId is required");
        this.roomId = Objects.requireNonNull(roomId, "roomId is required");
        this.checkInDate = Objects.requireNonNull(checkInDate, "checkInDate is required");
        this.checkOutDate = Objects.requireNonNull(checkOutDate, "checkOutDate is required");
        if (!checkInDate.isBefore(checkOutDate)) {
            throw new IllegalArgumentException("checkInDate must be before checkOutDate");
        }
        this.status = Objects.requireNonNull(status, "status is required");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
    }

    public Long getId() {
        return id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public Long getRoomId() {
        return roomId;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
