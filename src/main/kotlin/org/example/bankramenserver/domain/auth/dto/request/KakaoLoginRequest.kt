package org.example.bankramenserver.domain.auth.dto.request
import jakarta.validation.constraints.NotBlank
data class KakaoLoginRequest(@field:NotBlank(message = "카카오 액세스 토큰은 필수 항목입니다.") val kakaoAccessToken: String) { fun kakaoAccessToken() = kakaoAccessToken }
