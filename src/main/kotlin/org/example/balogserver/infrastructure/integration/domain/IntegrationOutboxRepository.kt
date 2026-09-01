package org.example.balogserver.infrastructure.integration.domain

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface IntegrationOutboxRepository : JpaRepository<IntegrationOutbox, UUID> {
    fun findTop50ByStatusInOrderByCreatedAtAsc(statuses: List<IntegrationOutbox.Status>?): List<IntegrationOutbox>
}
