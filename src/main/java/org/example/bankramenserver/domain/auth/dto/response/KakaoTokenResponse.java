package org.example.bankramenserver.domain.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "카카오 토큰 응답")
@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoTokenResponse(
        @Schema(description = "액세스 토큰", example = "eyJhbGci...")
        @JsonProperty("access_token")  String accessToken,
        @Schema(description = "토큰 타입", example = "bearer")
        @JsonProperty("token_type")    String tokenType,
        @Schema(description = "리프레시 토큰", example = "def456...")
        @JsonProperty("refresh_token") String refreshToken,
        @Schema(description = "토큰 유효 시간(초)", example = "21599")
        @JsonProperty("expires_in")    int expiresIn
) {}