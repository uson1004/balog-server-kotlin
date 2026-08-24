package org.example.bankramenserver.domain.auth.dto.response;

public record AuthTokenResponse(
        String accessToken,
        String refreshToken
) {
}
