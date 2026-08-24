package org.example.bankramenserver.infrastructure.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.bankramenserver.domain.transaction.event.PaymentTransactionRecordedEvent;
import org.example.bankramenserver.infrastructure.integration.domain.IntegrationConnectionProperties;
import org.example.bankramenserver.infrastructure.integration.domain.IntegrationOutbox;
import org.example.bankramenserver.infrastructure.integration.domain.IntegrationOutboxRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IntegrationOutboxWriter {

    public static final String TRANSACTION_CREATED = "transaction.created";

    private final IntegrationOutboxRepository outboxRepository;
    private final IntegrationConnectionProperties connectionProperties;
    private final ObjectMapper objectMapper;

    public void record(PaymentTransactionRecordedEvent event) {
        String payload = serialize(event);
        connectionProperties.getConnections().stream()
                .filter(connection -> connection.subscribesTo(TRANSACTION_CREATED))
                .forEach(connection -> outboxRepository.save(IntegrationOutbox.pending(
                        event.transactionId().toString(),
                        event.eventId().toString(),
                        connection.connectionId(),
                        connection.connectorType(),
                        TRANSACTION_CREATED,
                        payload
                )));
    }

    private String serialize(PaymentTransactionRecordedEvent event) {
        try {
            return objectMapper.writeValueAsString(new TransactionCreatedPayload(
                    event.eventId().toString(),
                    TRANSACTION_CREATED,
                    event.userId().toString(),
                    new TransactionPayload(
                            event.transactionId().toString(),
                            event.title(),
                            event.amount(),
                            event.category().getDisplayName(),
                            event.occurredAt().toString()
                    )
            ));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize integration event", exception);
        }
    }

    private record TransactionCreatedPayload(
            String eventId,
            @com.fasterxml.jackson.annotation.JsonProperty("event_type") String eventType,
            String userId,
            TransactionPayload transaction
    ) {
    }

    private record TransactionPayload(String id, String title, Long amount, String category, String occurredAt) {
    }
}
