package org.example.balogserver.domain.auth.service

import org.example.balogserver.domain.auth.dto.response.AuthTokenResponse
import org.example.balogserver.domain.user.config.SingleUserProperties
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("local")
class LocalAuthService(
    private val jwtService: JwtService,
    private val refreshTokenService: RefreshTokenService,
    private val singleUserProperties: SingleUserProperties,
) {
    fun login(): AuthTokenResponse {
        val userId = singleUserProperties.id
        val refreshToken = jwtService.generateRefreshToken(userId)
        refreshTokenService.save(refreshToken, userId)
        return AuthTokenResponse(jwtService.generateAccessToken(userId), refreshToken)
    }
}
