package org.example.bankramenserver.infrastructure.integration.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "integration")
public class IntegrationConnectionProperties {

    private List<Connection> connections = List.of();

    public Optional<Connection> findByConnectionId(String connectionId) {
        return connections.stream().filter(connection -> connection.connectionId().equals(connectionId)).findFirst();
    }

    public record Connection(
            String connectionId,
            String consumerId,
            String connectorType,
            boolean enabled,
            List<String> subscribedEvents,
            String webhookUrl,
            String secret
    ) {
        public boolean subscribesTo(String eventType) {
            return enabled && StringUtils.hasText(webhookUrl) && StringUtils.hasText(secret)
                    && subscribedEvents.contains(eventType);
        }
    }
}
