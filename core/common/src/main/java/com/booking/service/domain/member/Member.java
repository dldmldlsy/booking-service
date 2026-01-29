package com.booking.service.domain.member;

import java.util.Objects;

/**
 * 회원 도메인 뼈대. 추후 확장 예정.
 */
public class Member {
    private final Long id;
    private final String name;

    public Member(Long id, String name) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.name = Objects.requireNonNull(name, "name is required");
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
