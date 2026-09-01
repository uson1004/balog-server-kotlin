package org.example.balogserver.domain.auth.dto.response
data class AuthTokenResponse(val accessToken: String, val refreshToken: String) { fun accessToken() = accessToken; fun refreshToken() = refreshToken }
