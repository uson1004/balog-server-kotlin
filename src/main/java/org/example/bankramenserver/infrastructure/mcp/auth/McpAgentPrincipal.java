package org.example.bankramenserver.infrastructure.mcp.auth;

import java.util.Set;
import java.util.UUID;

public record McpAgentPrincipal(
        String connectionId,
        String consumerId,
        UUID linkedUserId,
        Set<AgentScope> scopes
) {

    public boolean hasScope(AgentScope scope) {
        return scopes.contains(scope);
    }
}
