package org.example.balogserver.infrastructure.integration

import org.assertj.core.api.Assertions.assertThat
import org.example.balogserver.infrastructure.integration.domain.IntegrationConnectionProperties
import org.example.balogserver.infrastructure.integration.domain.IntegrationOutbox
import org.example.balogserver.infrastructure.integration.domain.IntegrationOutboxRepository
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.LocalDateTime

class IntegrationOutboxDispatcherTest {
    private val outboxRepository = mock(IntegrationOutboxRepository::class.java)
    private val hermesConnector = mock(IntegrationConnector::class.java)

    @Test
    fun dispatchesPendingEntryThroughItsConnectionAndMarksItDelivered() {
        val properties = connections()
        val outbox = IntegrationOutbox.pending(
            "transaction-1", "event-1", "hermes-personal", "HERMES_WEBHOOK", "transaction.created", "{\"event_type\":\"transaction.created\"}",
        )
        `when`(outboxRepository.findTop50ByStatusInOrderByCreatedAtAsc(listOf(IntegrationOutbox.Status.PENDING, IntegrationOutbox.Status.RETRYING)))
            .thenReturn(listOf(outbox))
        `when`(hermesConnector.connectorType()).thenReturn("HERMES_WEBHOOK")

        IntegrationOutboxDispatcher(outboxRepository, properties, listOf(hermesConnector)).dispatchDue()

        verify(hermesConnector).dispatch(outbox, properties.connections.first())
        assertThat(outbox.status).isEqualTo(IntegrationOutbox.Status.DELIVERED)
    }

    @Test
    fun keepsFailedDeliveryForRetryInsteadOfThrowing() {
        val properties = connections()
        val outbox = IntegrationOutbox.pending(
            "transaction-1", "event-1", "hermes-personal", "HERMES_WEBHOOK", "transaction.created", "{}",
        )
        `when`(outboxRepository.findTop50ByStatusInOrderByCreatedAtAsc(listOf(IntegrationOutbox.Status.PENDING, IntegrationOutbox.Status.RETRYING)))
            .thenReturn(listOf(outbox))
        `when`(hermesConnector.connectorType()).thenReturn("HERMES_WEBHOOK")
        doThrow(IllegalStateException("offline")).`when`(hermesConnector).dispatch(outbox, properties.connections.first())

        IntegrationOutboxDispatcher(outboxRepository, properties, listOf(hermesConnector)).dispatchDue()

        assertThat(outbox.status).isEqualTo(IntegrationOutbox.Status.RETRYING)
        assertThat(outbox.attemptCount).isEqualTo(1)
        assertThat(outbox.nextAttemptAt).isAfter(LocalDateTime.now().minusSeconds(1))
    }

    private fun connections() = IntegrationConnectionProperties().apply {
        connections = listOf(
            IntegrationConnectionProperties.Connection(
                "hermes-personal", "yuseob-finance-assistant", "HERMES_WEBHOOK", true,
                listOf("transaction.created"), "http://localhost:8644/webhooks/balog-transactions", "test-secret",
            ),
        )
    }
}
