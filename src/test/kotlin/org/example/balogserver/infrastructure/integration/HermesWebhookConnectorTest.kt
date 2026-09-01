package org.example.balogserver.infrastructure.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.sun.net.httpserver.HttpServer
import org.assertj.core.api.Assertions.assertThat
import org.example.balogserver.domain.category.domain.Category
import org.example.balogserver.domain.transaction.event.PaymentTransactionRecordedEvent
import org.example.balogserver.infrastructure.integration.domain.IntegrationConnectionProperties
import org.example.balogserver.infrastructure.integration.domain.IntegrationOutbox
import org.example.balogserver.infrastructure.integration.domain.IntegrationOutboxRepository
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

class HermesWebhookConnectorTest {
    @Test
    fun sendsWriterPayloadWithEventIdAsIdempotencyHeader() {
        val body = AtomicReference<String>()
        val signature = AtomicReference<String>()
        val requestId = AtomicReference<String>()
        val server = HttpServer.create(InetSocketAddress(0), 0)
        server.createContext("/hook") { exchange ->
            body.set(String(exchange.requestBody.readAllBytes(), StandardCharsets.UTF_8))
            signature.set(exchange.requestHeaders.getFirst("X-Hub-Signature-256"))
            requestId.set(exchange.requestHeaders.getFirst("X-Request-ID"))
            exchange.sendResponseHeaders(202, -1)
            exchange.close()
        }
        server.start()
        try {
            val connection = IntegrationConnectionProperties.Connection(
                "hermes-personal", "consumer", "HERMES_WEBHOOK", true, listOf("transaction.created"),
                "http://127.0.0.1:${server.address.port}/hook", "test-secret",
            )
            val properties = IntegrationConnectionProperties().apply { connections = listOf(connection) }
            val outboxRepository = mock(IntegrationOutboxRepository::class.java)
            val writer = IntegrationOutboxWriter(outboxRepository, properties, ObjectMapper())
            val transactionId = UUID.fromString("22222222-2222-2222-2222-222222222222")
            val eventId = UUID.fromString("33333333-3333-3333-3333-333333333333")
            writer.record(
                PaymentTransactionRecordedEvent(
                    UUID.fromString("11111111-1111-1111-1111-111111111111"),
                    transactionId,
                    eventId,
                    "스타벅스",
                    4500L,
                    Category.CAFE_SNACK,
                    LocalDate.of(2026, 7, 11),
                ),
            )
            val captor = ArgumentCaptor.forClass(IntegrationOutbox::class.java)
            verify(outboxRepository).save(captor.capture())

            HermesWebhookConnector().dispatch(captor.value, connection)

            val payload = ObjectMapper().readTree(body.get())
            assertThat(payload.path("eventId").asText()).isEqualTo(eventId.toString())
            assertThat(payload.path("transaction").path("id").asText()).isEqualTo(transactionId.toString())
            assertThat(payload.path("eventId").asText()).isNotEqualTo(payload.path("transaction").path("id").asText())
            assertThat(requestId.get()).isEqualTo(payload.path("eventId").asText())
            assertThat(signature.get()).isEqualTo(HermesWebhookConnector.signature(body.get().toByteArray(StandardCharsets.UTF_8), "test-secret"))
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun signsTheExactUtf8PayloadBytesWithHmacSha256() {
        assertThat(HermesWebhookConnector.signature("payload".toByteArray(), "test-secret"))
            .isEqualTo("sha256=2fcd0dbc44d5dd073ead5ea4b4d81cfd543e5de42e9c353f80452715e2b576a3")
    }
}
