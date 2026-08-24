package org.example.bankramenserver.infrastructure.mcp.auth;

import lombok.RequiredArgsConstructor;
import org.example.bankramenserver.domain.user.domain.repository.UserRepository;
import org.example.bankramenserver.global.error.exception.ErrorCode;
import org.example.bankramenserver.global.error.exception.GlobalException;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class McpAgentConnectionAuthenticationService {

    private final AgentConnectionRepository agentConnectionRepository;
    private final UserRepository userRepository;

    public McpAgentPrincipal authenticate(String token) {
        AgentConnection connection = agentConnectionRepository.findByTokenHash(McpTokenHasher.hash(token))
                .filter(AgentConnection::isEnabled)
                .filter(candidate -> candidate.getLinkedUserId() != null)
                .filter(candidate -> userRepository.existsById(candidate.getLinkedUserId()))
                .orElseThrow(() -> new GlobalException(ErrorCode.INVALID_TOKEN));

        return new McpAgentPrincipal(
                connection.getConnectionId(),
                connection.getConsumerId(),
                connection.getLinkedUserId(),
                Set.copyOf(connection.getScopes())
        );
    }
}
