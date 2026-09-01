package org.example.balogserver.domain.auth.service
import org.example.balogserver.domain.auth.exception.InvalidTokenException
import org.example.balogserver.global.jwt.JwtProperties
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.TimeUnit
@Service class RefreshTokenService(private val redisTemplate: RedisTemplate<String, String>, private val jwtProperties: JwtProperties) { fun save(refreshToken: String, userId: UUID) { redisTemplate.opsForValue().set("refresh:$refreshToken", userId.toString(), jwtProperties.refreshExp, TimeUnit.SECONDS) }; fun validate(refreshToken: String, userId: UUID) { if (redisTemplate.opsForValue().get("refresh:$refreshToken") != userId.toString()) throw InvalidTokenException.EXCEPTION }; fun delete(refreshToken: String) { redisTemplate.delete("refresh:$refreshToken") } }
