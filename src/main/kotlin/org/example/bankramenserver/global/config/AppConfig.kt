package org.example.bankramenserver.global.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import java.time.Clock
import java.time.Duration

@Configuration
class AppConfig {
    @Bean fun restTemplate(builder: RestTemplateBuilder): RestTemplate = builder.setConnectTimeout(Duration.ofSeconds(3)).setReadTimeout(Duration.ofSeconds(5)).build()
    @Bean fun objectMapper(): ObjectMapper = ObjectMapper().registerModule(JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    @Bean fun clock(): Clock = Clock.systemDefaultZone()
}
