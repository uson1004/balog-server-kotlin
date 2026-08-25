package org.example.bankramenserver.infrastructure.integration.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Lob
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.example.bankramenserver.global.common.BaseEntity
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "integration_outbox", uniqueConstraints = [UniqueConstraint(name = "uk_integration_outbox_transaction_connection", columnNames = ["transaction_id", "connection_id"])])
class IntegrationOutbox protected constructor() : BaseEntity() {
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.UUID)
    @field:Column(columnDefinition = "BINARY(16)")
    final var id: UUID? = null
        private set
    @field:Column(name = "transaction_id", nullable = false, updatable = false)
    final lateinit var transactionId: String
        private set
    @field:Column(name = "event_id", nullable = false, updatable = false)
    final lateinit var eventId: String
        private set
    @field:Column(name = "connection_id", nullable = false, updatable = false)
    final lateinit var connectionId: String
        private set
    @field:Column(name = "connector_type", nullable = false, updatable = false)
    final lateinit var connectorType: String
        private set
    @field:Column(name = "event_type", nullable = false, updatable = false)
    final lateinit var eventType: String
        private set
    @field:Lob
    @field:Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    final lateinit var payload: String
        private set
    @field:Enumerated(EnumType.STRING)
    @field:Column(name = "status", nullable = false)
    final lateinit var status: Status
        private set
    @field:Column(name = "attempt_count", nullable = false)
    final var attemptCount = 0
        private set
    @field:Column(name = "next_attempt_at")
    final var nextAttemptAt: LocalDateTime? = null
        private set
    @field:Column(name = "last_error", columnDefinition = "TEXT")
    final var lastError: String? = null
        private set
    @field:Column(name = "delivered_at")
    final var deliveredAt: LocalDateTime? = null
        private set

    private constructor(transactionId: String, eventId: String, connectionId: String, connectorType: String, eventType: String, payload: String) : this() {
        this.transactionId = transactionId
        this.eventId = eventId
        this.connectionId = connectionId
        this.connectorType = connectorType
        this.eventType = eventType
        this.payload = payload
        status = Status.PENDING
        attemptCount = 0
    }

    fun isPending() = status == Status.PENDING || status == Status.RETRYING

    fun markDelivered() {
        status = Status.DELIVERED
        deliveredAt = LocalDateTime.now()
        nextAttemptAt = null
        lastError = null
    }

    fun markForRetry(exception: Exception) {
        attemptCount++
        status = Status.RETRYING
        nextAttemptAt = LocalDateTime.now().plusMinutes(minOf(1L shl minOf(attemptCount, 6), 60))
        lastError = "${exception.javaClass.simpleName}: ${exception.message}"
    }

    companion object {
        @JvmStatic fun pending(transactionId: String, eventId: String, connectionId: String, connectorType: String, eventType: String, payload: String) = IntegrationOutbox(transactionId, eventId, connectionId, connectorType, eventType, payload)
    }

    enum class Status { PENDING, RETRYING, DELIVERED }
}
