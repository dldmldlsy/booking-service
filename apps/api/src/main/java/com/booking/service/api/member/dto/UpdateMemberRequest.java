package com.booking.service.api.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateMemberRequest(
        @NotBlank @Size(min = 2, max = 30) String nickname) {
}
