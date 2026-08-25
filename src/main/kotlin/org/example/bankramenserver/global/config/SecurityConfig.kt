package org.example.bankramenserver.global.config

import org.example.bankramenserver.global.error.GlobalExceptionFilter
import org.example.bankramenserver.global.jwt.JwtAuthenticationFilter
import org.example.bankramenserver.infrastructure.mcp.auth.McpBearerAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(private val jwtAuthenticationFilter: JwtAuthenticationFilter, private val mcpBearerAuthenticationFilter: McpBearerAuthenticationFilter, private val globalExceptionFilter: GlobalExceptionFilter) {
    @Bean fun corsConfigurationSource(): CorsConfigurationSource = UrlBasedCorsConfigurationSource().also { source -> source.registerCorsConfiguration("/**", CorsConfiguration().also { configuration -> configuration.addAllowedOriginPattern("*"); configuration.addAllowedHeader("*"); configuration.addAllowedMethod("*"); configuration.allowCredentials = false }) }
    @Bean fun filterChain(http: HttpSecurity) = http.csrf { it.disable() }.cors { it.configurationSource(corsConfigurationSource()) }.sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }.authorizeHttpRequests { auth -> auth.requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll().requestMatchers("/auth/kakao/**").permitAll().requestMatchers("/reports/**", "/transactions/**", "/categories/**", "/push-notifications/**").authenticated().requestMatchers("/mcp").authenticated().anyRequest().authenticated() }.formLogin { it.disable() }.httpBasic { it.disable() }.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java).addFilterBefore(mcpBearerAuthenticationFilter, JwtAuthenticationFilter::class.java).addFilterBefore(globalExceptionFilter, McpBearerAuthenticationFilter::class.java).build()
}
