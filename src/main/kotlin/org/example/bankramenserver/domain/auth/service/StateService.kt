package org.example.bankramenserver.domain.auth.service
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.TimeUnit
@Service class StateService(private val redisTemplate: RedisTemplate<String, String>, @Value("\${oauth.state.expiration}") private val stateExpiration: Long, @Value("\${oauth.state.prefix}") private val prefix: String) { fun generateState() = UUID.randomUUID().toString().also { redisTemplate.opsForValue().set(prefix + it, it, stateExpiration, TimeUnit.SECONDS) }; fun validateState(state: String) = redisTemplate.opsForValue().getAndDelete(prefix + state) != null }
