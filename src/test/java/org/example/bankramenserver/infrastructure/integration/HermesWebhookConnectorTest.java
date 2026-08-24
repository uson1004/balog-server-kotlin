package org.example.bankramenserver.infrastructure.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.example.bankramenserver.domain.category.domain.Category;
import org.example.bankramenserver.domain.transaction.event.PaymentTransactionRecordedEvent;
import org.example.bankramenserver.infrastructure.integration.domain.IntegrationConnectionProperties;
import org.example.bankramenserver.infrastructure.integration.domain.IntegrationOutbox;
import org.example.bankramenserver.infrastructure.integration.domain.IntegrationOutboxRepository;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class HermesWebhookConnectorTest {

    @Test
    void sendsWriterPayloadWithEventIdAsIdempotencyHeader() throws Exception {
        AtomicReference<String> body = new AtomicReference<>();
        AtomicReference<String> signature = new AtomicReference<>();
        AtomicReference<String> requestId = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/hook", exchange -> {
            body.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            signature.set(exchange.getRequestHeaders().getFirst("X-Hub-Signature-256"));
            requestId.set(exchange.getRequestHeaders().getFirst("X-Request-ID"));
            exchange.sendResponseHeaders(202, -1);
            exchange.close();
        });
        server.start();
        try {
            IntegrationConnectionProperties.Connection connection = new IntegrationConnectionProperties.Connection(
                    "hermes-personal", "consumer", "HERMES_WEBHOOK", true, List.of("transaction.created"),
                    "http://127.0.0.1:" + server.getAddress().getPort() + "/hook", "test-secret"
            );
            IntegrationConnectionProperties properties = new IntegrationConnectionProperties();
            properties.setConnections(List.of(connection));
            IntegrationOutboxRepository outboxRepository = mock(IntegrationOutboxRepository.class);
            IntegrationOutboxWriter writer = new IntegrationOutboxWriter(outboxRepository, properties, new ObjectMapper());
            UUID transactionId = UUID.fromString("22222222-2222-2222-2222-222222222222");
            UUID eventId = UUID.fromString("33333333-3333-3333-3333-333333333333");
            writer.record(new PaymentTransactionRecordedEvent(
                    UUID.fromString("11111111-1111-1111-1111-111111111111"),
                    transactionId,
                    eventId,
                    "스타벅스",
                    4500L,
                    Category.CAFE_SNACK,
                    LocalDate.of(2026, 7, 11)
            ));
            org.mockito.ArgumentCaptor<IntegrationOutbox> outboxCaptor = org.mockito.ArgumentCaptor.forClass(IntegrationOutbox.class);
            verify(outboxRepository).save(outboxCaptor.capture());
            IntegrationOutbox outbox = outboxCaptor.getValue();

            new HermesWebhookConnector().dispatch(outbox, connection);

            JsonNode payload = new ObjectMapper().readTree(body.get());
            assertThat(payload.path("eventId").asText()).isEqualTo(eventId.toString());
            assertThat(payload.path("transaction").path("id").asText()).isEqualTo(transactionId.toString());
            assertThat(payload.path("eventId").asText()).isNotEqualTo(payload.path("transaction").path("id").asText());
            assertThat(requestId.get()).isEqualTo(payload.path("eventId").asText());
            assertThat(signature.get()).isEqualTo(HermesWebhookConnector.signature(body.get().getBytes(StandardCharsets.UTF_8), "test-secret"));
        } finally {
            server.stop(0);
        }
    }

    @Test
    void signsTheExactUtf8PayloadBytesWithHmacSha256() {
        assertThat(HermesWebhookConnector.signature("payload".getBytes(), "test-secret"))
                .isEqualTo("sha256=2fcd0dbc44d5dd073ead5ea4b4d81cfd543e5de42e9c353f80452715e2b576a3");
    }
}
