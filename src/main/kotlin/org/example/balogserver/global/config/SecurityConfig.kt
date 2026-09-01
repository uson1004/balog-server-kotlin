package org.example.balogserver.global.config

import org.example.balogserver.global.error.GlobalExceptionFilter
import org.example.balogserver.infrastructure.mcp.auth.McpBearerAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val mcpBearerAuthenticationFilter: McpBearerAuthenticationFilter,
    private val globalExceptionFilter: GlobalExceptionFilter,
) {
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource = UrlBasedCorsConfigurationSource().also { source ->
        source.registerCorsConfiguration("/**", CorsConfiguration().also {
            it.addAllowedOriginPattern("*")
            it.addAllowedHeader("*")
            it.addAllowedMethod("*")
            it.allowCredentials = false
        })
    }

    @Bean
    fun filterChain(http: HttpSecurity) = http
        .csrf { it.disable() }
        .cors { it.configurationSource(corsConfigurationSource()) }
        .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
        .authorizeHttpRequests { it.requestMatchers("/mcp").authenticated().anyRequest().permitAll() }
        .formLogin { it.disable() }
        .httpBasic { it.disable() }
        .addFilterBefore(mcpBearerAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
        .addFilterBefore(globalExceptionFilter, McpBearerAuthenticationFilter::class.java)
        .build()
}
