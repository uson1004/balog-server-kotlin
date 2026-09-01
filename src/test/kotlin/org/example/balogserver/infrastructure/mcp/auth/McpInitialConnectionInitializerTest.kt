package org.example.balogserver.infrastructure.mcp.auth

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.example.balogserver.domain.user.domain.repository.UserRepository
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.boot.DefaultApplicationArguments
import java.util.Optional
import java.util.UUID

class McpInitialConnectionInitializerTest {
    private val agentConnectionRepository = mock(AgentConnectionRepository::class.java)
    private val userRepository = mock(UserRepository::class.java)

    @Test
    fun updatesAnExistingConfiguredConnectionOnStartup() {
        val properties = properties("rotated-token-12345678901234567890").apply { enabled = false }
        val connection = AgentConnection.of(
            "hermes-local", "hermes-agent", LINKED_USER_ID, true, McpTokenHasher.hash("old-token"), setOf(AgentScope.TRANSACTIONS_READ),
        )
        `when`(agentConnectionRepository.findById("hermes-local")).thenReturn(Optional.of(connection))
        `when`(userRepository.existsById(LINKED_USER_ID)).thenReturn(true)

        McpInitialConnectionInitializer(properties, agentConnectionRepository, userRepository).run(DefaultApplicationArguments())

        assertThat(connection.enabled).isFalse()
        assertThat(connection.tokenHash).isEqualTo(McpTokenHasher.hash("rotated-token-12345678901234567890"))
        assertThat(connection.scopes).containsExactlyInAnyOrder(AgentScope.TRANSACTIONS_READ, AgentScope.REPORTS_READ)
    }

    @Test
    fun rejectsAnInitialTokenThatIsTooShort() {
        val properties = properties("too-short")

        assertThatThrownBy {
            McpInitialConnectionInitializer(properties, agentConnectionRepository, userRepository).run(DefaultApplicationArguments())
        }.isInstanceOf(IllegalStateException::class.java)
            .hasMessage("MCP_INITIAL_TOKEN must contain at least 32 characters")
    }

    private fun properties(token: String) = McpInitialConnectionProperties().apply {
        connectionId = "hermes-local"
        consumerId = "hermes-agent"
        linkedUserId = LINKED_USER_ID.toString()
        this.token = token
    }

    private companion object {
        val LINKED_USER_ID: UUID = UUID.fromString("11111111-1111-1111-1111-111111111111")
    }
}
