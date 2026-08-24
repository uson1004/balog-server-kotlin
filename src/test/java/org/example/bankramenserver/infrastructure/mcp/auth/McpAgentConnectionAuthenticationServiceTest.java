package org.example.bankramenserver.infrastructure.mcp.auth;

import org.example.bankramenserver.domain.user.domain.repository.UserRepository;
import org.example.bankramenserver.global.error.exception.GlobalException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.example.bankramenserver.global.error.exception.ErrorCode.INVALID_TOKEN;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class McpAgentConnectionAuthenticationServiceTest {

    private static final UUID LINKED_USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Mock
    private AgentConnectionRepository agentConnectionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private McpAgentConnectionAuthenticationService authenticationService;

    @Test
    void authenticatesAnEnabledConnectionToItsLinkedUser() {
        // Given
        AgentConnection connection = AgentConnection.of(
                "hermes-local",
                "hermes-agent",
                LINKED_USER_ID,
                true,
                McpTokenHasher.hash("local-agent-token"),
                Set.of(AgentScope.TRANSACTIONS_READ, AgentScope.REPORTS_READ)
        );
        when(agentConnectionRepository.findByTokenHash(McpTokenHasher.hash("local-agent-token")))
                .thenReturn(java.util.Optional.of(connection));
        when(userRepository.existsById(LINKED_USER_ID)).thenReturn(true);

        // When
        McpAgentPrincipal principal = authenticationService.authenticate("local-agent-token");

        // Then
        assertThat(principal.linkedUserId()).isEqualTo(LINKED_USER_ID);
        assertThat(principal.scopes()).containsExactlyInAnyOrder(
                AgentScope.TRANSACTIONS_READ,
                AgentScope.REPORTS_READ
        );
    }

    @Test
    void rejectsUnknownOrDisabledConnections() {
        // Given
        when(agentConnectionRepository.findByTokenHash(anyString())).thenReturn(java.util.Optional.empty());
        when(agentConnectionRepository.findByTokenHash(McpTokenHasher.hash("disabled-token")))
                .thenReturn(java.util.Optional.of(AgentConnection.of(
                        "hermes-disabled",
                        "hermes-agent",
                        LINKED_USER_ID,
                        false,
                        McpTokenHasher.hash("disabled-token"),
                        Set.of(AgentScope.TRANSACTIONS_READ)
                )));

        // When / Then
        assertThatThrownBy(() -> authenticationService.authenticate("unknown-token"))
                .isInstanceOf(GlobalException.class)
                .extracting(error -> ((GlobalException) error).getErrorCode())
                .isEqualTo(INVALID_TOKEN);

        assertThatThrownBy(() -> authenticationService.authenticate("disabled-token"))
                .isInstanceOf(GlobalException.class)
                .extracting(error -> ((GlobalException) error).getErrorCode())
                .isEqualTo(INVALID_TOKEN);
    }

    @Test
    void rejectsAConnectionWhoseLinkedUserIsMissing() {
        when(agentConnectionRepository.findByTokenHash(McpTokenHasher.hash("orphan-token")))
                .thenReturn(java.util.Optional.of(AgentConnection.of(
                        "hermes-orphan",
                        "hermes-agent",
                        LINKED_USER_ID,
                        true,
                        McpTokenHasher.hash("orphan-token"),
                        Set.of(AgentScope.REPORTS_READ)
                )));
        when(userRepository.existsById(LINKED_USER_ID)).thenReturn(false);

        assertThatThrownBy(() -> authenticationService.authenticate("orphan-token"))
                .isInstanceOf(GlobalException.class)
                .extracting(error -> ((GlobalException) error).getErrorCode())
                .isEqualTo(INVALID_TOKEN);
    }
}
