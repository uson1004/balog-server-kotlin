package org.example.bankramenserver.infrastructure.mcp.auth

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.example.bankramenserver.global.error.exception.ErrorCode
import org.example.bankramenserver.global.error.exception.GlobalException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

@Component
class McpBearerAuthenticationFilter(
    private val authenticationService: McpAgentConnectionAuthenticationService,
) : OncePerRequestFilter() {
    override fun shouldNotFilter(request: HttpServletRequest): Boolean = request.requestURI != "/mcp"

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val principal = authenticationService.authenticate(resolveToken(request))
        SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(
            principal,
            null,
            principal.scopes.map { SimpleGrantedAuthority("SCOPE_${it.name}") },
        )
        filterChain.doFilter(request, response)
    }

    private fun resolveToken(request: HttpServletRequest): String {
        val authorization = request.getHeader("Authorization")
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            throw GlobalException(ErrorCode.MISSING_TOKEN)
        }
        return authorization.substring(7).takeIf(StringUtils::hasText) ?: throw GlobalException(ErrorCode.MISSING_TOKEN)
    }
}
