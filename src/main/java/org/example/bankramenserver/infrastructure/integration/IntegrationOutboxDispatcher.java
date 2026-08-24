package org.example.bankramenserver.infrastructure.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bankramenserver.infrastructure.integration.domain.IntegrationConnectionProperties;
import org.example.bankramenserver.infrastructure.integration.domain.IntegrationOutbox;
import org.example.bankramenserver.infrastructure.integration.domain.IntegrationOutboxRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class IntegrationOutboxDispatcher {

    private final IntegrationOutboxRepository outboxRepository;
    private final IntegrationConnectionProperties connectionProperties;
    private final List<IntegrationConnector> connectors;

    @Transactional
    public void dispatchDue() {
        LocalDateTime now = LocalDateTime.now();
        outboxRepository.findTop50ByStatusInOrderByCreatedAtAsc(List.of(
                        IntegrationOutbox.Status.PENDING,
                        IntegrationOutbox.Status.RETRYING
                )).stream()
                .filter(outbox -> outbox.getNextAttemptAt() == null || !outbox.getNextAttemptAt().isAfter(now))
                .forEach(this::dispatch);
    }

    private void dispatch(IntegrationOutbox outbox) {
        try {
            IntegrationConnectionProperties.Connection connection = connectionProperties
                    .findByConnectionId(outbox.getConnectionId())
                    .orElseThrow(() -> new IllegalStateException("Integration connection is unavailable"));
            if (!connection.enabled()) {
                throw new IllegalStateException("Integration connection is disabled");
            }
            connectors.stream()
                    .filter(connector -> connector.connectorType().equals(outbox.getConnectorType()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Integration connector is unavailable"))
                    .dispatch(outbox, connection);
            outbox.markDelivered();
        } catch (Exception exception) {
            outbox.markForRetry(exception);
            log.warn("Integration delivery failed: outboxId={}, connectionId={}", outbox.getId(), outbox.getConnectionId(), exception);
        }
    }
}
