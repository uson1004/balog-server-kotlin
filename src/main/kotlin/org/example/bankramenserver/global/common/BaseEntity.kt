package org.example.bankramenserver.global.common

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity {
    @field:CreatedDate
    @field:Column(nullable = false, updatable = false)
    final var createdAt: LocalDateTime? = null
        private set

    @field:LastModifiedDate
    @field:Column(nullable = false)
    final var updatedAt: LocalDateTime? = null
        private set
}
