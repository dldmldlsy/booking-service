package com.booking.service.domain.member;

import java.util.Objects;

/**
 * 회원 도메인 모델.
 */
public class Member {
    private final Long id;
    private final String email;
    private final String nickname;
    private final String passwordHash;

    public Member(Long id, String email, String nickname, String passwordHash) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.email = Objects.requireNonNull(email, "email is required");
        this.nickname = Objects.requireNonNull(nickname, "nickname is required");
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash is required");
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getNickname() {
        return nickname;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}
