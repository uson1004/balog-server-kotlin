package org.example.bankramenserver.global.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.example.bankramenserver.domain.auth.service.JwtService
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

@Component
class JwtAuthenticationFilter(private val jwtService: JwtService) : OncePerRequestFilter() {
    override fun shouldNotFilter(request: HttpServletRequest): Boolean { val path = request.requestURI; return path == "/mcp" || PERMIT_URLS.any(path::startsWith) }
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) { resolveToken(request)?.let { token -> val userId: UUID = jwtService.validateAccessToken(token); SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(userId, null, emptyList()) }; filterChain.doFilter(request, response) }
    private fun resolveToken(request: HttpServletRequest): String? = request.getHeader("Authorization")?.takeIf { StringUtils.hasText(it) && it.startsWith("Bearer ") }?.substring(7)
    companion object { private val PERMIT_URLS = arrayOf("/auth/kakao/login", "/auth/kakao/callback", "/swagger-ui", "/v3/api-docs", "/swagger-resources", "/webjars") }
}
