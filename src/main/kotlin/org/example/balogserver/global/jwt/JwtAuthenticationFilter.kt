package org.example.balogserver.global.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.example.balogserver.domain.auth.service.JwtService
import org.example.balogserver.domain.auth.exception.InvalidTokenException
import org.example.balogserver.domain.user.domain.repository.UserRepository
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val userRepository: UserRepository,
) : OncePerRequestFilter() {
    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI.removePrefix(request.contextPath)
        return path == "/mcp" || PUBLIC_PATHS.any { path == it || path.startsWith("$it/") }
    }

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        request.getHeader("Authorization")?.let { authorization ->
            if (!authorization.startsWith("Bearer ", ignoreCase = true)) throw InvalidTokenException.EXCEPTION
            val token = authorization.substring(7)
            if (token.isBlank()) throw InvalidTokenException.EXCEPTION
            val userId = jwtService.validateAccessToken(token)
            if (!userRepository.existsById(userId)) throw InvalidTokenException.EXCEPTION
            SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(userId, null, emptyList())
        }
        filterChain.doFilter(request, response)
    }

    private companion object {
        val PUBLIC_PATHS = listOf("/swagger-ui", "/v3/api-docs", "/swagger-resources", "/webjars")
    }
}
