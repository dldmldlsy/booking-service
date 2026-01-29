package com.booking.service.domain.accommodation;

import java.util.Objects;

/**
 * 숙박 상품(숙소) 정보를 나타내는 도메인 모델.
 */
public class Accommodation {
    private final Long id;
    private final String name;
    private final String address;
    private final String description;

    public Accommodation(Long id, String name, String address, String description) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.name = Objects.requireNonNull(name, "name is required");
        this.address = Objects.requireNonNull(address, "address is required");
        this.description = description == null ? "" : description;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getDescription() {
        return description;
    }
}
