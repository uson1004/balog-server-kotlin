package org.example.balogserver.domain.auth.service

import org.assertj.core.api.Assertions.assertThat
import org.example.balogserver.domain.user.config.SingleUserProperties
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class LocalAuthServiceTest {
    @Mock lateinit var jwtService: JwtService
    @Mock lateinit var refreshTokenService: RefreshTokenService
    private val properties = SingleUserProperties()

    @Test
    fun `issues tokens for the fixed local user`() {
        val userId = UUID.fromString("00000000-0000-4000-8000-000000000001")
        `when`(jwtService.generateAccessToken(userId)).thenReturn("access-token")
        `when`(jwtService.generateRefreshToken(userId)).thenReturn("refresh-token")

        val response = LocalAuthService(jwtService, refreshTokenService, properties).login()

        assertThat(response.accessToken).isEqualTo("access-token")
        assertThat(response.refreshToken).isEqualTo("refresh-token")
        verify(refreshTokenService).save("refresh-token", userId)
    }
}
