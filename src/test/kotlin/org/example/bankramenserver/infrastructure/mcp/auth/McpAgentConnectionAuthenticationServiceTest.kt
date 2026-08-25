package org.example.bankramenserver.infrastructure.mcp.auth

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.example.bankramenserver.domain.user.domain.repository.UserRepository
import org.example.bankramenserver.global.error.exception.ErrorCode
import org.example.bankramenserver.global.error.exception.GlobalException
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.Optional
import java.util.UUID

class McpAgentConnectionAuthenticationServiceTest {
    private val agentConnectionRepository = mock(AgentConnectionRepository::class.java)
    private val userRepository = mock(UserRepository::class.java)
    private val authenticationService = McpAgentConnectionAuthenticationService(agentConnectionRepository, userRepository)

    @Test
    fun authenticatesAnEnabledConnectionToItsLinkedUser() {
        val connection = AgentConnection.of(
            "hermes-local", "hermes-agent", LINKED_USER_ID, true, McpTokenHasher.hash("local-agent-token"),
            setOf(AgentScope.TRANSACTIONS_READ, AgentScope.REPORTS_READ),
        )
        `when`(agentConnectionRepository.findByTokenHash(McpTokenHasher.hash("local-agent-token"))).thenReturn(Optional.of(connection))
        `when`(userRepository.existsById(LINKED_USER_ID)).thenReturn(true)

        val principal = authenticationService.authenticate("local-agent-token")

        assertThat(principal.linkedUserId).isEqualTo(LINKED_USER_ID)
        assertThat(principal.scopes).containsExactlyInAnyOrder(AgentScope.TRANSACTIONS_READ, AgentScope.REPORTS_READ)
    }

    @Test
    fun rejectsUnknownOrDisabledConnections() {
        `when`(agentConnectionRepository.findByTokenHash(org.mockito.ArgumentMatchers.anyString())).thenReturn(Optional.empty())
        `when`(agentConnectionRepository.findByTokenHash(McpTokenHasher.hash("disabled-token"))).thenReturn(
            Optional.of(AgentConnection.of("hermes-disabled", "hermes-agent", LINKED_USER_ID, false, McpTokenHasher.hash("disabled-token"), setOf(AgentScope.TRANSACTIONS_READ))),
        )

        assertThatThrownBy { authenticationService.authenticate("unknown-token") }
            .isInstanceOf(GlobalException::class.java)
            .extracting { (it as GlobalException).errorCode }
            .isEqualTo(ErrorCode.INVALID_TOKEN)
        assertThatThrownBy { authenticationService.authenticate("disabled-token") }
            .isInstanceOf(GlobalException::class.java)
            .extracting { (it as GlobalException).errorCode }
            .isEqualTo(ErrorCode.INVALID_TOKEN)
    }

    @Test
    fun rejectsAConnectionWhoseLinkedUserIsMissing() {
        `when`(agentConnectionRepository.findByTokenHash(McpTokenHasher.hash("orphan-token"))).thenReturn(
            Optional.of(AgentConnection.of("hermes-orphan", "hermes-agent", LINKED_USER_ID, true, McpTokenHasher.hash("orphan-token"), setOf(AgentScope.REPORTS_READ))),
        )
        `when`(userRepository.existsById(LINKED_USER_ID)).thenReturn(false)

        assertThatThrownBy { authenticationService.authenticate("orphan-token") }
            .isInstanceOf(GlobalException::class.java)
            .extracting { (it as GlobalException).errorCode }
            .isEqualTo(ErrorCode.INVALID_TOKEN)
    }

    private companion object {
        val LINKED_USER_ID: UUID = UUID.fromString("11111111-1111-1111-1111-111111111111")
    }
}
