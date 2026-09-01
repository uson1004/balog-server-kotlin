package org.example.balogserver.infrastructure.integration

import org.example.balogserver.infrastructure.integration.domain.IntegrationConnectionProperties
import org.example.balogserver.infrastructure.integration.domain.IntegrationOutbox
import org.example.balogserver.infrastructure.integration.domain.IntegrationOutboxRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class IntegrationOutboxDispatcher(
    private val outboxRepository: IntegrationOutboxRepository,
    private val connectionProperties: IntegrationConnectionProperties,
    private val connectors: List<IntegrationConnector>,
) {
    @Transactional
    fun dispatchDue() {
        val now = LocalDateTime.now()
        outboxRepository.findTop50ByStatusInOrderByCreatedAtAsc(
            listOf(IntegrationOutbox.Status.PENDING, IntegrationOutbox.Status.RETRYING),
        ).filter { it.nextAttemptAt?.isAfter(now) != true }
            .forEach(::dispatch)
    }

    private fun dispatch(outbox: IntegrationOutbox) {
        try {
            val connection = connectionProperties.findByConnectionId(outbox.connectionId)
                ?: throw IllegalStateException("Integration connection is unavailable")
            if (!connection.enabled) throw IllegalStateException("Integration connection is disabled")
            connectors.firstOrNull { it.connectorType() == outbox.connectorType }
                ?.dispatch(outbox, connection)
                ?: throw IllegalStateException("Integration connector is unavailable")
            outbox.markDelivered()
        } catch (exception: Exception) {
            outbox.markForRetry(exception)
            logger.warn(
                "Integration delivery failed: outboxId={}, connectionId={}",
                outbox.id,
                outbox.connectionId,
                exception,
            )
        }
    }

    private companion object {
        val logger = LoggerFactory.getLogger(IntegrationOutboxDispatcher::class.java)
    }
}
