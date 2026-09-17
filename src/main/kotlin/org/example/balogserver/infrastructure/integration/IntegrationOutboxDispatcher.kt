package org.example.balogserver.infrastructure.integration

import org.example.balogserver.infrastructure.integration.domain.IntegrationConnectionProperties
import org.example.balogserver.infrastructure.integration.domain.IntegrationOutbox
import org.example.balogserver.infrastructure.integration.domain.IntegrationOutboxRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDateTime
import java.util.UUID

@Component
class IntegrationOutboxDispatcher(
    private val outboxRepository: IntegrationOutboxRepository,
    private val connectionProperties: IntegrationConnectionProperties,
    private val connectors: List<IntegrationConnector>,
    transactionManager: PlatformTransactionManager,
) {
    private val transactionTemplate = TransactionTemplate(transactionManager)

    fun dispatchDue() {
        repeat(BATCH_SIZE) {
            val outbox = claimNextDue() ?: return
            dispatch(outbox)
        }
    }

    private fun claimNextDue(): IntegrationOutbox? = transactionTemplate.execute {
        val now = LocalDateTime.now()
        outboxRepository.findFirstClaimableAt(
            now,
            IntegrationOutbox.Status.PENDING,
            IntegrationOutbox.Status.RETRYING,
            IntegrationOutbox.Status.PROCESSING,
            PageRequest.of(0, 1),
        )
            .firstOrNull()
            ?.also { it.claim(UUID.randomUUID(), now.plusMinutes(LEASE_MINUTES)) }
    }

    private fun dispatch(outbox: IntegrationOutbox) {
        try {
            val connection = connectionProperties.findByConnectionId(outbox.connectionId)
                ?: throw IllegalStateException("Integration connection is unavailable")
            if (!connection.enabled) throw IllegalStateException("Integration connection is disabled")
            connectors.firstOrNull { it.connectorType() == outbox.connectorType }
                ?.dispatch(outbox, connection)
                ?: throw IllegalStateException("Integration connector is unavailable")
            markDelivered(outbox)
        } catch (exception: Exception) {
            markForRetry(outbox, exception)
            logger.warn(
                "Integration delivery failed: outboxId={}, connectionId={}",
                outbox.id,
                outbox.connectionId,
                exception,
            )
        }
    }

    private fun markDelivered(claimedOutbox: IntegrationOutbox) = complete(claimedOutbox) { it.markDelivered() }

    private fun markForRetry(claimedOutbox: IntegrationOutbox, exception: Exception) = complete(claimedOutbox) { it.markForRetry(exception) }

    private fun complete(claimedOutbox: IntegrationOutbox, update: (IntegrationOutbox) -> Unit) = transactionTemplate.executeWithoutResult {
        val outboxId = checkNotNull(claimedOutbox.id)
        val leaseId = checkNotNull(claimedOutbox.leaseId)
        outboxRepository.findByIdAndLeaseIdAndStatus(outboxId, leaseId, IntegrationOutbox.Status.PROCESSING)
            .ifPresent(update)
    }

    private companion object {
        const val BATCH_SIZE = 50
        const val LEASE_MINUTES = 5L
        val logger = LoggerFactory.getLogger(IntegrationOutboxDispatcher::class.java)
    }
}
