package org.example.balogserver.infrastructure.integration

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.Table
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
import java.time.LocalDate
import java.util.UUID

class IntegrationOutboxWriterTest {
    private val outboxRepository = mock(IntegrationOutboxRepository::class.java)

    @Test
    fun recordsOnePendingDeliveryForEachEnabledSubscribedConnection() {
        val properties = IntegrationConnectionProperties().apply {
            connections = listOf(
                IntegrationConnectionProperties.Connection(
                    "hermes-personal", "yuseob-finance-assistant", "HERMES_WEBHOOK", true,
                    listOf("transaction.created"), "http://localhost:8644/webhooks/balog-transactions", "test-secret",
                ),
                IntegrationConnectionProperties.Connection(
                    "disabled", "disabled-agent", "HERMES_WEBHOOK", false,
                    listOf("transaction.created"), "http://localhost/disabled", "test-secret",
                ),
            )
        }
        val transactionId = UUID.fromString("22222222-2222-2222-2222-222222222222")
        val eventId = UUID.fromString("33333333-3333-3333-3333-333333333333")
        IntegrationOutboxWriter(outboxRepository, properties, ObjectMapper()).record(
            PaymentTransactionRecordedEvent(
                UUID.fromString("11111111-1111-1111-1111-111111111111"), transactionId, eventId, "스타벅스", 4500L,
                Category.CAFE_SNACK, LocalDate.of(2026, 7, 11),
            ),
        )

        val captor = ArgumentCaptor.forClass(IntegrationOutbox::class.java)
        verify(outboxRepository).save(captor.capture())
        val outbox = captor.value
        val payload = ObjectMapper().readTree(outbox.payload)
        assertThat(outbox.connectionId).isEqualTo("hermes-personal")
        assertThat(outbox.transactionId).isEqualTo(transactionId.toString())
        assertThat(outbox.eventId).isEqualTo(eventId.toString())
        assertThat(outbox.isPending()).isTrue()
        assertThat(payload.path("eventId").asText()).isEqualTo(eventId.toString())
        assertThat(payload.path("event_type").asText()).isEqualTo("transaction.created")
        assertThat(payload.path("transaction").path("id").asText()).isEqualTo(transactionId.toString())
        assertThat(payload.path("transaction").path("amount").asLong()).isEqualTo(4500L)
    }

    @Test
    fun deduplicatesOutboxEntriesByTransactionAndConnection() {
        val table = IntegrationOutbox::class.java.getAnnotation(Table::class.java)
        assertThat(table.uniqueConstraints).hasSize(1)
        assertThat(table.uniqueConstraints[0].columnNames).containsExactly("transaction_id", "connection_id")
    }
}
