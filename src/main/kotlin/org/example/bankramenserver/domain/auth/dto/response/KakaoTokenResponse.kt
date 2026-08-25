package org.example.bankramenserver.domain.auth.dto.response
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
@JsonIgnoreProperties(ignoreUnknown = true)
data class KakaoTokenResponse(@JsonProperty("access_token") val accessToken: String, @JsonProperty("token_type") val tokenType: String, @JsonProperty("refresh_token") val refreshToken: String, @JsonProperty("expires_in") val expiresIn: Int) { fun accessToken() = accessToken; fun tokenType() = tokenType; fun refreshToken() = refreshToken; fun expiresIn() = expiresIn }
