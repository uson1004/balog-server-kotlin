package org.example.bankramenserver.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class StateService {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${oauth.state.expiration}")
    private long stateExpiration;

    @Value("${oauth.state.prefix}")
    private String PREFIX;

    public String generateState() {
        String state = UUID.randomUUID().toString();
        redisTemplate.opsForValue()
                .set(PREFIX + state, state, stateExpiration, TimeUnit.SECONDS);
        return state;
    }

    public boolean validateState(String state) {
        String key = PREFIX + state;

        String saved = redisTemplate.opsForValue().getAndDelete(key);

        return saved != null;
    }
}