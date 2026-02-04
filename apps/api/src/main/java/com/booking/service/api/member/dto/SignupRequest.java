package com.booking.service.api.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @Email @NotBlank String email,
        @NotBlank @Size(min = 2, max = 30) String nickname,
        @NotBlank @Size(min = 8, max = 100) String password) {
}
