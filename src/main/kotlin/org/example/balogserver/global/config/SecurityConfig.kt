package org.example.balogserver.global.config

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.DispatcherType
import org.example.balogserver.global.error.GlobalExceptionFilter
import org.example.balogserver.global.error.ErrorResponse
import org.example.balogserver.global.error.exception.ErrorCode
import org.example.balogserver.global.jwt.JwtAuthenticationFilter
import org.example.balogserver.infrastructure.mcp.auth.McpBearerAuthenticationFilter
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
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
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val objectMapper: ObjectMapper,
    private val environment: Environment,
) {
    @Bean
    fun jwtServletRegistration() = FilterRegistrationBean(jwtAuthenticationFilter).apply { isEnabled = false }

    @Bean
    fun mcpServletRegistration() = FilterRegistrationBean(mcpBearerAuthenticationFilter).apply { isEnabled = false }

    @Bean
    fun exceptionServletRegistration() = FilterRegistrationBean(globalExceptionFilter).apply { isEnabled = false }

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
        .authorizeHttpRequests {
            it.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()
            if (environment.acceptsProfiles(Profiles.of("local"))) {
                it.requestMatchers(HttpMethod.GET, "/auth/local/login").permitAll()
            }
            it.anyRequest().authenticated()
        }
        .exceptionHandling {
            it.authenticationEntryPoint { _, response, _ ->
                response.status = ErrorCode.MISSING_TOKEN.status.value()
                response.contentType = MediaType.APPLICATION_JSON_VALUE
                response.characterEncoding = "UTF-8"
                objectMapper.writeValue(response.writer, ErrorResponse.of(ErrorCode.MISSING_TOKEN))
            }
        }
        .formLogin { it.disable() }
        .httpBasic { it.disable() }
        .addFilterBefore(mcpBearerAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
        .addFilterBefore(jwtAuthenticationFilter, McpBearerAuthenticationFilter::class.java)
        .addFilterBefore(globalExceptionFilter, JwtAuthenticationFilter::class.java)
        .build()
}
