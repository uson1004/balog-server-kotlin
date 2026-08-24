package org.example.bankramenserver.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenRequest(
        @NotBlank(message = "리프레시 토큰은 필수 항목입니다.")
        String refreshToken
) {}
