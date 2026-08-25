package org.example.bankramenserver.infrastructure.mcp.auth

import java.util.UUID

data class McpAgentPrincipal(
    val connectionId: String,
    val consumerId: String,
    val linkedUserId: UUID,
    val scopes: Set<AgentScope>,
) {
    fun hasScope(scope: AgentScope): Boolean = scope in scopes
}
