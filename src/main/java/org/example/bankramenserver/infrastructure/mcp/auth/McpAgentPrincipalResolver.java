package org.example.bankramenserver.infrastructure.mcp.auth;

import org.example.bankramenserver.global.error.exception.ErrorCode;
import org.example.bankramenserver.global.error.exception.GlobalException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class McpAgentPrincipalResolver {

    public McpAgentPrincipal requireScope(AgentScope scope) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof McpAgentPrincipal principal)) {
            throw new GlobalException(ErrorCode.INVALID_TOKEN);
        }
        if (!principal.hasScope(scope)) {
            throw new McpScopeDeniedException();
        }
        return principal;
    }
}
