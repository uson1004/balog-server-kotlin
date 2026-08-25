package org.example.bankramenserver.infrastructure.mcp.auth

import org.example.bankramenserver.domain.user.domain.repository.UserRepository
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import java.util.EnumSet
import java.util.UUID

@Component
class McpInitialConnectionInitializer(
    private val properties: McpInitialConnectionProperties,
    private val agentConnectionRepository: AgentConnectionRepository,
    private val userRepository: UserRepository,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments) {
        val token = properties.token
        if (!StringUtils.hasText(token)) return
        check(token!!.length >= 32) { "MCP_INITIAL_TOKEN must contain at least 32 characters" }
        val connectionId = properties.connectionId
        val consumerId = properties.consumerId
        check(StringUtils.hasText(connectionId) && StringUtils.hasText(consumerId)) {
            "MCP_CONNECTION_ID and MCP_CONSUMER_ID are required when MCP_INITIAL_TOKEN is configured"
        }
        val linkedUserId = parseLinkedUserId()
        check(userRepository.existsById(linkedUserId)) { "MCP_LINKED_USER_ID must identify an existing Bankramen user" }
        val connection = agentConnectionRepository.findById(connectionId!!).orElseGet {
            AgentConnection.of(
                connectionId,
                consumerId!!,
                linkedUserId,
                properties.enabled,
                McpTokenHasher.hash(token),
                EnumSet.of(AgentScope.TRANSACTIONS_READ, AgentScope.REPORTS_READ),
            )
        }
        connection.syncInitialConnection(consumerId!!, linkedUserId, properties.enabled, McpTokenHasher.hash(token))
        agentConnectionRepository.save(connection)
    }

    private fun parseLinkedUserId(): UUID {
        val linkedUserId = properties.linkedUserId
        check(StringUtils.hasText(linkedUserId)) { "MCP_LINKED_USER_ID is required when MCP_INITIAL_TOKEN is configured" }
        return try {
            UUID.fromString(linkedUserId)
        } catch (exception: IllegalArgumentException) {
            throw IllegalStateException("MCP_LINKED_USER_ID must be a UUID when MCP_INITIAL_TOKEN is configured", exception)
        }
    }
}
