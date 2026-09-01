package org.example.balogserver.infrastructure.mcp.auth

import org.example.balogserver.global.error.exception.ErrorCode
import org.example.balogserver.global.error.exception.GlobalException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class McpAgentPrincipalResolver {
    fun requireScope(scope: AgentScope): McpAgentPrincipal {
        val principal = SecurityContextHolder.getContext().authentication?.principal as? McpAgentPrincipal
            ?: throw GlobalException(ErrorCode.INVALID_TOKEN)
        if (!principal.hasScope(scope)) throw McpScopeDeniedException()
        return principal
    }
}
