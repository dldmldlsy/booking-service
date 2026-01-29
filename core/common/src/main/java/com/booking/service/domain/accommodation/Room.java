package com.booking.service.domain.accommodation;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 숙소의 개별 객실 정보를 나타내는 도메인 모델.
 */
public class Room {
    private final Long id;
    private final Long accommodationId;
    private final String name;
    private final int capacity;
    private final BigDecimal basePrice;

    public Room(Long id, Long accommodationId, String name, int capacity, BigDecimal basePrice) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.accommodationId = Objects.requireNonNull(accommodationId, "accommodationId is required");
        this.name = Objects.requireNonNull(name, "name is required");
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        this.capacity = capacity;
        this.basePrice = Objects.requireNonNull(basePrice, "basePrice is required");
        if (basePrice.signum() < 0) {
            throw new IllegalArgumentException("basePrice must be >= 0");
        }
    }

    public Long getId() {
        return id;
    }

    public Long getAccommodationId() {
        return accommodationId;
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }
}
