package org.example.bankramenserver.infrastructure.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Table;
import org.example.bankramenserver.domain.category.domain.Category;
import org.example.bankramenserver.domain.transaction.event.PaymentTransactionRecordedEvent;
import org.example.bankramenserver.infrastructure.integration.domain.IntegrationConnectionProperties;
import org.example.bankramenserver.infrastructure.integration.domain.IntegrationOutbox;
import org.example.bankramenserver.infrastructure.integration.domain.IntegrationOutboxRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class IntegrationOutboxWriterTest {

    @Mock
    private IntegrationOutboxRepository outboxRepository;

    @Test
    void recordsOnePendingDeliveryForEachEnabledSubscribedConnection() throws Exception {
        IntegrationConnectionProperties properties = new IntegrationConnectionProperties();
        properties.setConnections(List.of(
                new IntegrationConnectionProperties.Connection(
                        "hermes-personal", "yuseob-finance-assistant", "HERMES_WEBHOOK", true,
                        List.of("transaction.created"), "http://localhost:8644/webhooks/bankramen-transactions", "test-secret"
                ),
                new IntegrationConnectionProperties.Connection(
                        "disabled", "disabled-agent", "HERMES_WEBHOOK", false,
                        List.of("transaction.created"), "http://localhost/disabled", "test-secret"
                )
        ));
        IntegrationOutboxWriter writer = new IntegrationOutboxWriter(outboxRepository, properties, new ObjectMapper());
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID transactionId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID eventId = UUID.fromString("33333333-3333-3333-3333-333333333333");

        writer.record(new PaymentTransactionRecordedEvent(
                userId, transactionId, eventId, "스타벅스", 4500L, Category.CAFE_SNACK, LocalDate.of(2026, 7, 11)
        ));

        ArgumentCaptor<IntegrationOutbox> captor = ArgumentCaptor.forClass(IntegrationOutbox.class);
        verify(outboxRepository).save(captor.capture());
        IntegrationOutbox outbox = captor.getValue();
        JsonNode payload = new ObjectMapper().readTree(outbox.getPayload());
        assertThat(outbox.getConnectionId()).isEqualTo("hermes-personal");
        assertThat(outbox.getTransactionId()).isEqualTo(transactionId.toString());
        assertThat(outbox.getEventId()).isEqualTo(eventId.toString());
        assertThat(outbox.isPending()).isTrue();
        assertThat(payload.path("eventId").asText()).isEqualTo(eventId.toString());
        assertThat(payload.path("event_type").asText()).isEqualTo("transaction.created");
        assertThat(payload.path("transaction").path("id").asText()).isEqualTo(transactionId.toString());
        assertThat(payload.path("transaction").path("amount").asLong()).isEqualTo(4500L);
    }

    @Test
    void deduplicatesOutboxEntriesByTransactionAndConnection() {
        Table table = IntegrationOutbox.class.getAnnotation(Table.class);

        assertThat(table.uniqueConstraints()).hasSize(1);
        assertThat(table.uniqueConstraints()[0].columnNames())
                .containsExactly("transaction_id", "connection_id");
    }

}
