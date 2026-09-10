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
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.SimpleTransactionStatus
import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

class IntegrationOutboxDispatcherTest {
    private val outboxRepository = mock(IntegrationOutboxRepository::class.java)
    private val hermesConnector = mock(IntegrationConnector::class.java)
    private val transactionManager = mock(PlatformTransactionManager::class.java).also {
        `when`(it.getTransaction(org.mockito.ArgumentMatchers.any(TransactionDefinition::class.java)))
            .thenReturn(SimpleTransactionStatus())
    }

    @Test
    fun dispatchesPendingEntryThroughItsConnectionAndMarksItDelivered() {
        val properties = connections()
        val outbox = IntegrationOutbox.pending(
            "transaction-1", "event-1", "hermes-personal", "HERMES_WEBHOOK", "transaction.created", "{\"event_type\":\"transaction.created\"}",
        )
        ReflectionTestUtils.setField(outbox, "id", UUID.randomUUID())
        `when`(outboxRepository.findFirstClaimableAt(anyTime(), pendingStatus(), retryingStatus(), processingStatus()))
            .thenReturn(outbox, null)
        `when`(outboxRepository.findByIdAndLeaseIdAndStatus(anyUuid(), anyUuid(), processingStatus()))
            .thenReturn(Optional.of(outbox))
        `when`(hermesConnector.connectorType()).thenReturn("HERMES_WEBHOOK")

        IntegrationOutboxDispatcher(outboxRepository, properties, listOf(hermesConnector), transactionManager).dispatchDue()

        verify(hermesConnector).dispatch(outbox, properties.connections.first())
        assertThat(outbox.status).isEqualTo(IntegrationOutbox.Status.DELIVERED)
    }

    @Test
    fun keepsFailedDeliveryForRetryInsteadOfThrowing() {
        val properties = connections()
        val outbox = IntegrationOutbox.pending(
            "transaction-1", "event-1", "hermes-personal", "HERMES_WEBHOOK", "transaction.created", "{}",
        )
        ReflectionTestUtils.setField(outbox, "id", UUID.randomUUID())
        `when`(outboxRepository.findFirstClaimableAt(anyTime(), pendingStatus(), retryingStatus(), processingStatus()))
            .thenReturn(outbox, null)
        `when`(outboxRepository.findByIdAndLeaseIdAndStatus(anyUuid(), anyUuid(), processingStatus()))
            .thenReturn(Optional.of(outbox))
        `when`(hermesConnector.connectorType()).thenReturn("HERMES_WEBHOOK")
        doThrow(IllegalStateException("offline")).`when`(hermesConnector).dispatch(outbox, properties.connections.first())

        IntegrationOutboxDispatcher(outboxRepository, properties, listOf(hermesConnector), transactionManager).dispatchDue()

        assertThat(outbox.status).isEqualTo(IntegrationOutbox.Status.RETRYING)
        assertThat(outbox.attemptCount).isEqualTo(1)
        assertThat(outbox.nextAttemptAt).isAfter(LocalDateTime.now().minusSeconds(1))
    }

    @Test
    fun makesAnExpiredLeaseClaimable() {
        val outbox = IntegrationOutbox.pending(
            "transaction-1", "event-1", "hermes-personal", "HERMES_WEBHOOK", "transaction.created", "{}",
        )
        val firstLease = outbox.claim(UUID.randomUUID(), LocalDateTime.now().minusSeconds(1))

        assertThat(outbox.isClaimableAt(LocalDateTime.now())).isTrue()

        val secondLease = outbox.claim(UUID.randomUUID(), LocalDateTime.now().plusMinutes(5))
        assertThat(secondLease).isNotEqualTo(firstLease)
    }

    private fun connections() = IntegrationConnectionProperties().apply {
        connections = listOf(
            IntegrationConnectionProperties.Connection(
                "hermes-personal", "yuseob-finance-assistant", "HERMES_WEBHOOK", true,
                listOf("transaction.created"), "http://localhost:8644/webhooks/balog-transactions", "test-secret",
            ),
        )
    }

    private fun anyUuid(): UUID = org.mockito.ArgumentMatchers.any(UUID::class.java) ?: UUID.randomUUID()

    private fun anyTime(): LocalDateTime = org.mockito.ArgumentMatchers.any(LocalDateTime::class.java) ?: LocalDateTime.now()

    private fun pendingStatus(): IntegrationOutbox.Status =
        org.mockito.ArgumentMatchers.eq(IntegrationOutbox.Status.PENDING) ?: IntegrationOutbox.Status.PENDING

    private fun retryingStatus(): IntegrationOutbox.Status =
        org.mockito.ArgumentMatchers.eq(IntegrationOutbox.Status.RETRYING) ?: IntegrationOutbox.Status.RETRYING

    private fun processingStatus(): IntegrationOutbox.Status =
        org.mockito.ArgumentMatchers.eq(IntegrationOutbox.Status.PROCESSING) ?: IntegrationOutbox.Status.PROCESSING
}
