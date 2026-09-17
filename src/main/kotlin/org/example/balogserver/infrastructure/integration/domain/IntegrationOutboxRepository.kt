package org.example.balogserver.infrastructure.integration.domain

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime
import java.util.UUID

interface IntegrationOutboxRepository : JpaRepository<IntegrationOutbox, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        """
        select outbox from IntegrationOutbox outbox
        where outbox.status = :pending
            or (outbox.status = :retrying and (outbox.nextAttemptAt is null or outbox.nextAttemptAt <= :now))
            or (outbox.status = :processing and (outbox.leaseExpiresAt is null or outbox.leaseExpiresAt <= :now))
        order by outbox.createdAt asc
        """,
    )
    fun findFirstClaimableAt(
        @Param("now") now: LocalDateTime,
        @Param("pending") pending: IntegrationOutbox.Status,
        @Param("retrying") retrying: IntegrationOutbox.Status,
        @Param("processing") processing: IntegrationOutbox.Status,
        pageable: Pageable,
    ): List<IntegrationOutbox>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findByIdAndLeaseIdAndStatus(id: UUID, leaseId: UUID, status: IntegrationOutbox.Status): java.util.Optional<IntegrationOutbox>
}
