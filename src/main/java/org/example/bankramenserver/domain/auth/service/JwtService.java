package org.example.bankramenserver.domain.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.example.bankramenserver.domain.auth.exception.ExpiredTokenException;
import org.example.bankramenserver.domain.auth.exception.InvalidTokenException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long accessExpSeconds;
    private final long refreshExpSeconds;

    public JwtService(
            @Value("${jwt.secretKey}") String secret,
            @Value("${jwt.accessExp}") long accessExpSeconds,
            @Value("${jwt.refreshExp}") long refreshExpSeconds
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessExpSeconds = accessExpSeconds;
        this.refreshExpSeconds = refreshExpSeconds;
    }

    public String generateAccessToken(UUID userId) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("type", "access")
                .id(UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(new Date(
                        System.currentTimeMillis()
                                + TimeUnit.SECONDS.toMillis(accessExpSeconds)
                ))
                .signWith(secretKey)
                .compact();
    }

    public String generateRefreshToken(UUID userId) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("type", "refresh")
                .id(UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(new Date(
                        System.currentTimeMillis()
                                + TimeUnit.SECONDS.toMillis(refreshExpSeconds)
                ))
                .signWith(secretKey)
                .compact();
    }

    public UUID validateAccessToken(String token) {
        Claims claims = parse(token);

        if (!"access".equals(claims.get("type"))) {
            throw InvalidTokenException.EXCEPTION;
        }

        return UUID.fromString(claims.getSubject());
    }

    public UUID validateRefreshToken(String token) {
        Claims claims = parse(token);

        if (!"refresh".equals(claims.get("type"))) {
            throw InvalidTokenException.EXCEPTION;
        }

        return UUID.fromString(claims.getSubject());
    }

    private Claims parse(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

        } catch (ExpiredJwtException e) {
            throw ExpiredTokenException.EXCEPTION;
        } catch (Exception e) {
            throw InvalidTokenException.EXCEPTION;
        }
    }
}