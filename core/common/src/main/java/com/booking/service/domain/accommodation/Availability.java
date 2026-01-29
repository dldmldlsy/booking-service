package com.booking.service.domain.accommodation;

import java.time.LocalDate;
import java.util.Objects;

/**
 * 객실별 일자별 남은 수량을 나타내는 도메인 모델.
 */
public class Availability {
    private final Long id;
    private final Long roomId;
    private final LocalDate date;
    private int availableCount;

    public Availability(Long id, Long roomId, LocalDate date, int availableCount) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.roomId = Objects.requireNonNull(roomId, "roomId is required");
        this.date = Objects.requireNonNull(date, "date is required");
        if (availableCount < 0) {
            throw new IllegalArgumentException("availableCount must be >= 0");
        }
        this.availableCount = availableCount;
    }

    public Long getId() {
        return id;
    }

    public Long getRoomId() {
        return roomId;
    }

    public LocalDate getDate() {
        return date;
    }

    public int getAvailableCount() {
        return availableCount;
    }

    public void decrement() {
        if (availableCount <= 0) {
            throw new IllegalStateException("No availability left");
        }
        availableCount--;
    }

    public void increment() {
        availableCount++;
    }
}
