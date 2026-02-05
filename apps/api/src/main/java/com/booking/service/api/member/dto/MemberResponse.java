package com.booking.service.api.member.dto;

import com.booking.service.domain.member.Member;

public record MemberResponse(Long id, String email, String nickname, String role) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(member.getId(), member.getEmail(), member.getNickname(), member.getRole().name());
    }
}
