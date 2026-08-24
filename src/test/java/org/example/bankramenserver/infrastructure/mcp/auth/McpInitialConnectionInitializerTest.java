package org.example.bankramenserver.infrastructure.mcp.auth;

import org.example.bankramenserver.domain.user.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class McpInitialConnectionInitializerTest {

    private static final UUID LINKED_USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Mock
    private AgentConnectionRepository agentConnectionRepository;

    @Mock
    private UserRepository userRepository;

    private McpInitialConnectionInitializer initializer;

    @Test
    void updatesAnExistingConfiguredConnectionOnStartup() throws Exception {
        McpInitialConnectionProperties properties = new McpInitialConnectionProperties();
        properties.setConnectionId("hermes-local");
        properties.setConsumerId("hermes-agent");
        properties.setLinkedUserId(LINKED_USER_ID.toString());
        properties.setEnabled(false);
        properties.setToken("rotated-token-12345678901234567890");
        AgentConnection connection = AgentConnection.of(
                "hermes-local",
                "hermes-agent",
                LINKED_USER_ID,
                true,
                McpTokenHasher.hash("old-token"),
                Set.of(AgentScope.TRANSACTIONS_READ)
        );
        initializer = new McpInitialConnectionInitializer(properties, agentConnectionRepository, userRepository);
        when(agentConnectionRepository.findById("hermes-local")).thenReturn(Optional.of(connection));
        when(userRepository.existsById(LINKED_USER_ID)).thenReturn(true);

        initializer.run(new DefaultApplicationArguments());

        assertThat(connection.isEnabled()).isFalse();
        assertThat(connection.getTokenHash()).isEqualTo(McpTokenHasher.hash("rotated-token-12345678901234567890"));
        assertThat(connection.getScopes()).containsExactlyInAnyOrder(
                AgentScope.TRANSACTIONS_READ,
                AgentScope.REPORTS_READ
        );
    }

    @Test
    void rejectsAnInitialTokenThatIsTooShort() {
        McpInitialConnectionProperties properties = new McpInitialConnectionProperties();
        properties.setConnectionId("hermes-local");
        properties.setConsumerId("hermes-agent");
        properties.setLinkedUserId(LINKED_USER_ID.toString());
        properties.setToken("too-short");
        initializer = new McpInitialConnectionInitializer(properties, agentConnectionRepository, userRepository);

        assertThatThrownBy(() -> initializer.run(new DefaultApplicationArguments()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("MCP_INITIAL_TOKEN must contain at least 32 characters");
    }
}
