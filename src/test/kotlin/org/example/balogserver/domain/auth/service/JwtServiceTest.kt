package org.example.balogserver.domain.auth.service

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.example.balogserver.global.error.exception.GlobalException
import org.junit.jupiter.api.Test
import java.util.Date
import java.util.UUID

class JwtServiceTest {
    private val secret = "unit-test-only-signing-key-0123456789abcdef0123456789abcdef"
    private val service = JwtService(secret, 300, 600)

    @Test
    fun accessAndRefreshRoundTripWithDistinctTypes() {
        val userId = UUID.randomUUID()
        val access = service.generateAccessToken(userId)
        val refresh = service.generateRefreshToken(userId)
        assertThat(service.validateAccessToken(access)).isEqualTo(userId)
        assertThat(service.validateRefreshToken(refresh)).isEqualTo(userId)
        assertThatThrownBy { service.validateRefreshToken(access) }.isInstanceOf(GlobalException::class.java)
        assertThatThrownBy { service.validateAccessToken(refresh) }.isInstanceOf(GlobalException::class.java)
    }

    @Test
    fun missingExpirationIsRejected() {
        val token = Jwts.builder().subject(UUID.randomUUID().toString()).claim("type", "access")
            .signWith(Keys.hmacShaKeyFor(secret.toByteArray())).compact()
        assertThatThrownBy { service.validateAccessToken(token) }.isInstanceOf(GlobalException::class.java)
    }

    @Test
    fun missingSubjectIsRejected() {
        val token = Jwts.builder().claim("type", "access").expiration(Date(System.currentTimeMillis() + 60000))
            .signWith(Keys.hmacShaKeyFor(secret.toByteArray())).compact()
        assertThatThrownBy { service.validateAccessToken(token) }.isInstanceOf(GlobalException::class.java)
    }
}
