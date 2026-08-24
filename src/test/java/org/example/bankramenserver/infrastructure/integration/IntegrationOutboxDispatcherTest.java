package org.example.bankramenserver.infrastructure.integration;

import org.example.bankramenserver.infrastructure.integration.domain.IntegrationConnectionProperties;
import org.example.bankramenserver.infrastructure.integration.domain.IntegrationOutbox;
import org.example.bankramenserver.infrastructure.integration.domain.IntegrationOutboxRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IntegrationOutboxDispatcherTest {

    @Mock
    private IntegrationOutboxRepository outboxRepository;

    @Mock
    private IntegrationConnector hermesConnector;

    @Test
    void dispatchesPendingEntryThroughItsConnectionAndMarksItDelivered() throws Exception {
        IntegrationConnectionProperties properties = connections();
        IntegrationOutbox outbox = IntegrationOutbox.pending(
                "transaction-1", "event-1", "hermes-personal", "HERMES_WEBHOOK", "transaction.created", "{\"event_type\":\"transaction.created\"}"
        );
        when(outboxRepository.findTop50ByStatusInOrderByCreatedAtAsc(List.of(
                IntegrationOutbox.Status.PENDING, IntegrationOutbox.Status.RETRYING
        ))).thenReturn(List.of(outbox));
        when(hermesConnector.connectorType()).thenReturn("HERMES_WEBHOOK");
        IntegrationOutboxDispatcher dispatcher = new IntegrationOutboxDispatcher(
                outboxRepository, properties, List.of(hermesConnector)
        );

        dispatcher.dispatchDue();

        verify(hermesConnector).dispatch(outbox, properties.getConnections().get(0));
        assertThat(outbox.getStatus()).isEqualTo(IntegrationOutbox.Status.DELIVERED);
    }

    @Test
    void keepsFailedDeliveryForRetryInsteadOfThrowing() throws Exception {
        IntegrationConnectionProperties properties = connections();
        IntegrationOutbox outbox = IntegrationOutbox.pending(
                "transaction-1", "event-1", "hermes-personal", "HERMES_WEBHOOK", "transaction.created", "{}"
        );
        when(outboxRepository.findTop50ByStatusInOrderByCreatedAtAsc(List.of(
                IntegrationOutbox.Status.PENDING, IntegrationOutbox.Status.RETRYING
        ))).thenReturn(List.of(outbox));
        when(hermesConnector.connectorType()).thenReturn("HERMES_WEBHOOK");
        org.mockito.Mockito.doThrow(new IllegalStateException("offline"))
                .when(hermesConnector).dispatch(outbox, properties.getConnections().get(0));
        IntegrationOutboxDispatcher dispatcher = new IntegrationOutboxDispatcher(
                outboxRepository, properties, List.of(hermesConnector)
        );

        dispatcher.dispatchDue();

        assertThat(outbox.getStatus()).isEqualTo(IntegrationOutbox.Status.RETRYING);
        assertThat(outbox.getAttemptCount()).isEqualTo(1);
        assertThat(outbox.getNextAttemptAt()).isAfter(LocalDateTime.now().minusSeconds(1));
    }

    private IntegrationConnectionProperties connections() {
        IntegrationConnectionProperties properties = new IntegrationConnectionProperties();
        properties.setConnections(List.of(new IntegrationConnectionProperties.Connection(
                "hermes-personal", "yuseob-finance-assistant", "HERMES_WEBHOOK", true,
                List.of("transaction.created"), "http://localhost:8644/webhooks/bankramen-transactions", "test-secret"
        )));
        return properties;
    }
}
