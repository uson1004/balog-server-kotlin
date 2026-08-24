package org.example.bankramenserver.infrastructure.mcp.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.bankramenserver.global.error.exception.ErrorCode;
import org.example.bankramenserver.global.error.exception.GlobalException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class McpBearerAuthenticationFilter extends OncePerRequestFilter {

    private final McpAgentConnectionAuthenticationService authenticationService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"/mcp".equals(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        McpAgentPrincipal principal = authenticationService.authenticate(resolveToken(request));
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.scopes().stream()
                        .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope.name()))
                        .toList()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            throw new GlobalException(ErrorCode.MISSING_TOKEN);
        }
        String token = authorization.substring(7);
        if (!StringUtils.hasText(token)) {
            throw new GlobalException(ErrorCode.MISSING_TOKEN);
        }
        return token;
    }
}
