package org.example.bankramenserver.infrastructure.integration

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.example.bankramenserver.domain.transaction.event.PaymentTransactionRecordedEvent
import org.example.bankramenserver.infrastructure.integration.domain.IntegrationConnectionProperties
import org.example.bankramenserver.infrastructure.integration.domain.IntegrationOutbox
import org.example.bankramenserver.infrastructure.integration.domain.IntegrationOutboxRepository
import org.springframework.stereotype.Component

@Component
class IntegrationOutboxWriter(
    private val outboxRepository: IntegrationOutboxRepository,
    private val connectionProperties: IntegrationConnectionProperties,
    private val objectMapper: ObjectMapper,
) {
    fun record(event: PaymentTransactionRecordedEvent) {
        val payload = serialize(event)
        connectionProperties.connections.filter { it.subscribesTo(TRANSACTION_CREATED) }.forEach { connection ->
            outboxRepository.save(
                IntegrationOutbox.pending(
                    event.transactionId!!.toString(),
                    event.eventId.toString(),
                    connection.connectionId,
                    connection.connectorType,
                    TRANSACTION_CREATED,
                    payload,
                ),
            )
        }
    }

    private fun serialize(event: PaymentTransactionRecordedEvent): String = try {
        objectMapper.writeValueAsString(
            TransactionCreatedPayload(
                event.eventId.toString(),
                TRANSACTION_CREATED,
                event.userId.toString(),
                TransactionPayload(
                    event.transactionId.toString(),
                    event.title!!,
                    event.amount!!,
                    event.category!!.displayName,
                    event.occurredAt!!.toString(),
                ),
            ),
        )
    } catch (exception: JsonProcessingException) {
        throw IllegalStateException("Could not serialize integration event", exception)
    }

    private data class TransactionCreatedPayload(
        val eventId: String,
        @field:JsonProperty("event_type") val eventType: String,
        val userId: String,
        val transaction: TransactionPayload,
    )

    private data class TransactionPayload(
        val id: String,
        val title: String,
        val amount: Long,
        val category: String,
        val occurredAt: String,
    )

    companion object {
        const val TRANSACTION_CREATED = "transaction.created"
    }
}
