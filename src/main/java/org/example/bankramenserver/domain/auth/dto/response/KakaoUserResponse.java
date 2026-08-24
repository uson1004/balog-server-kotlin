package org.example.bankramenserver.domain.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "카카오 사용자 정보 응답")
@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoUserResponse(
        @Schema(description = "카카오 고유 ID", example = "123456789")
        Long id,
        @Schema(description = "카카오 계정 정보")
        @JsonProperty("kakao_account") KakaoAccount kakaoAccount
) {
    @Schema(description = "카카오 계정 세부 정보")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record KakaoAccount(
            @Schema(description = "사용자 이메일", example = "user@kakao.com")
            String email,
            @Schema(description = "프로필 정보")
            KakaoProfile profile
    ) {}

    @Schema(description = "카카오 프로필 정보")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record KakaoProfile(
            @Schema(description = "닉네임", example = "홍길동")
            String nickname,
            @Schema(description = "프로필 이미지 URL", example = "http://k.kakaocdn.net/...")
            @JsonProperty("profile_image_url") String profileImageUrl
    ) {}
}