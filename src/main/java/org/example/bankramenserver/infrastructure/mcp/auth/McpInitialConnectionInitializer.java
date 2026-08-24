package org.example.bankramenserver.infrastructure.mcp.auth;

import lombok.RequiredArgsConstructor;
import org.example.bankramenserver.domain.user.domain.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.EnumSet;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class McpInitialConnectionInitializer implements ApplicationRunner {

    private final McpInitialConnectionProperties properties;
    private final AgentConnectionRepository agentConnectionRepository;
    private final UserRepository userRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (!StringUtils.hasText(properties.getToken())) {
            return;
        }
        if (properties.getToken().length() < 32) {
            throw new IllegalStateException("MCP_INITIAL_TOKEN must contain at least 32 characters");
        }
        if (!StringUtils.hasText(properties.getConnectionId()) || !StringUtils.hasText(properties.getConsumerId())) {
            throw new IllegalStateException("MCP_CONNECTION_ID and MCP_CONSUMER_ID are required when MCP_INITIAL_TOKEN is configured");
        }
        UUID linkedUserId = parseLinkedUserId();
        if (!userRepository.existsById(linkedUserId)) {
            throw new IllegalStateException("MCP_LINKED_USER_ID must identify an existing Bankramen user");
        }
        AgentConnection connection = agentConnectionRepository.findById(properties.getConnectionId())
                .orElseGet(() -> AgentConnection.of(
                        properties.getConnectionId(),
                        properties.getConsumerId(),
                        linkedUserId,
                        properties.isEnabled(),
                        McpTokenHasher.hash(properties.getToken()),
                        EnumSet.of(AgentScope.TRANSACTIONS_READ, AgentScope.REPORTS_READ)
                ));
        connection.syncInitialConnection(
                properties.getConsumerId(),
                linkedUserId,
                properties.isEnabled(),
                McpTokenHasher.hash(properties.getToken())
        );
        agentConnectionRepository.save(connection);
    }

    private UUID parseLinkedUserId() {
        if (!StringUtils.hasText(properties.getLinkedUserId())) {
            throw new IllegalStateException("MCP_LINKED_USER_ID is required when MCP_INITIAL_TOKEN is configured");
        }
        try {
            return UUID.fromString(properties.getLinkedUserId());
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("MCP_LINKED_USER_ID must be a UUID when MCP_INITIAL_TOKEN is configured", exception);
        }
    }
}
