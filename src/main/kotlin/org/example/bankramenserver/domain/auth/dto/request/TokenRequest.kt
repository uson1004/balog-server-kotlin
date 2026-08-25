package org.example.bankramenserver.domain.auth.dto.request
import jakarta.validation.constraints.NotBlank
data class TokenRequest(@field:NotBlank(message = "리프레시 토큰은 필수 항목입니다.") val refreshToken: String) { fun refreshToken() = refreshToken }
