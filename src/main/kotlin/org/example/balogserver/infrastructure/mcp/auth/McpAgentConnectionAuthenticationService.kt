package org.example.balogserver.infrastructure.mcp.auth

import org.example.balogserver.domain.user.domain.repository.UserRepository
import org.example.balogserver.global.error.exception.ErrorCode
import org.example.balogserver.global.error.exception.GlobalException
import org.springframework.stereotype.Service

@Service
class McpAgentConnectionAuthenticationService(
    private val agentConnectionRepository: AgentConnectionRepository,
    private val userRepository: UserRepository,
) {
    fun authenticate(token: String): McpAgentPrincipal {
        val connection = agentConnectionRepository.findByTokenHash(McpTokenHasher.hash(token))
            .filter { it.enabled }
            .filter { it.linkedUserId != null }
            .filter { userRepository.existsById(it.linkedUserId!!) }
            .orElseThrow { GlobalException(ErrorCode.INVALID_TOKEN) }
        return McpAgentPrincipal(
            connection.connectionId,
            connection.consumerId,
            connection.linkedUserId!!,
            connection.scopes.toSet(),
        )
    }
}
