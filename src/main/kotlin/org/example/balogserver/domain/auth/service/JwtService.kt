package org.example.balogserver.domain.auth.service
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.example.balogserver.domain.auth.exception.ExpiredTokenException
import org.example.balogserver.domain.auth.exception.InvalidTokenException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.crypto.SecretKey
@Service
class JwtService(
    @param:Value("\${jwt.secretKey}") secret: String,
    @param:Value("\${jwt.accessExp}") private val accessExpSeconds: Long,
    @param:Value("\${jwt.refreshExp}") private val refreshExpSeconds: Long,
) {
    private val secretKey: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())

    fun generateAccessToken(userId: UUID) = generateToken(userId, "access", accessExpSeconds)
    fun generateRefreshToken(userId: UUID) = generateToken(userId, "refresh", refreshExpSeconds)
    fun validateAccessToken(token: String) = validate(token, "access")
    fun validateRefreshToken(token: String) = validate(token, "refresh")

    private fun generateToken(userId: UUID, type: String, expires: Long) = Jwts.builder()
        .subject(userId.toString()).claim("type", type).id(UUID.randomUUID().toString())
        .issuedAt(Date()).expiration(Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expires)))
        .signWith(secretKey).compact()

    private fun validate(token: String, type: String): UUID {
        val claims = parse(token)
        if (claims["type"] != type || claims.expiration == null) throw InvalidTokenException.EXCEPTION
        val subject = claims.subject ?: throw InvalidTokenException.EXCEPTION
        return try {
            UUID.fromString(subject)
        } catch (_: IllegalArgumentException) {
            throw InvalidTokenException.EXCEPTION
        }
    }

    private fun parse(token: String): Claims = try {
        Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).payload
    } catch (_: ExpiredJwtException) {
        throw ExpiredTokenException.EXCEPTION
    } catch (_: Exception) {
        throw InvalidTokenException.EXCEPTION
    }
}
