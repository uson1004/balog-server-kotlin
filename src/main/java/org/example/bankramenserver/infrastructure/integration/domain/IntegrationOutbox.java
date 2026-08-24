package org.example.bankramenserver.infrastructure.integration.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.bankramenserver.global.common.BaseEntity;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "integration_outbox", uniqueConstraints = @UniqueConstraint(
        name = "uk_integration_outbox_transaction_connection", columnNames = {"transaction_id", "connection_id"}
))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IntegrationOutbox extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "transaction_id", nullable = false, updatable = false)
    private String transactionId;

    @Column(name = "event_id", nullable = false, updatable = false)
    private String eventId;

    @Column(name = "connection_id", nullable = false, updatable = false)
    private String connectionId;

    @Column(name = "connector_type", nullable = false, updatable = false)
    private String connectorType;

    @Column(name = "event_type", nullable = false, updatable = false)
    private String eventType;

    @Lob
    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "next_attempt_at")
    private LocalDateTime nextAttemptAt;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    private IntegrationOutbox(String transactionId, String eventId, String connectionId, String connectorType, String eventType, String payload) {
        this.transactionId = transactionId;
        this.eventId = eventId;
        this.connectionId = connectionId;
        this.connectorType = connectorType;
        this.eventType = eventType;
        this.payload = payload;
        this.status = Status.PENDING;
        this.attemptCount = 0;
    }

    public static IntegrationOutbox pending(String transactionId, String eventId, String connectionId, String connectorType, String eventType, String payload) {
        return new IntegrationOutbox(transactionId, eventId, connectionId, connectorType, eventType, payload);
    }

    public boolean isPending() {
        return status == Status.PENDING || status == Status.RETRYING;
    }

    public void markDelivered() {
        status = Status.DELIVERED;
        deliveredAt = LocalDateTime.now();
        nextAttemptAt = null;
        lastError = null;
    }

    public void markForRetry(Exception exception) {
        attemptCount++;
        status = Status.RETRYING;
        nextAttemptAt = LocalDateTime.now().plusMinutes(Math.min(1L << Math.min(attemptCount, 6), 60));
        lastError = exception.getClass().getSimpleName() + ": " + exception.getMessage();
    }

    public enum Status {
        PENDING, RETRYING, DELIVERED
    }
}
