package org.example.bankramenserver.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.example.bankramenserver.domain.auth.exception.InvalidTokenException;
import org.example.bankramenserver.global.jwt.JwtProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String PREFIX = "refresh:";

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtProperties jwtProperties;

    public void save(String refreshToken, UUID userId) {
        redisTemplate.opsForValue().set(
                PREFIX + refreshToken,
                userId.toString(),
                jwtProperties.getRefreshExp(),
                TimeUnit.SECONDS
        );
    }

    public void validate(String refreshToken, UUID userId) {
        String storedUserId = redisTemplate.opsForValue().get(PREFIX + refreshToken);

        if (storedUserId == null || !storedUserId.equals(userId.toString())) {
            throw InvalidTokenException.EXCEPTION;
        }
    }

    public void delete(String refreshToken) {
        redisTemplate.delete(PREFIX + refreshToken);
    }
}