package org.example.bankramenserver.domain.push.domain.repository

import org.example.bankramenserver.domain.push.domain.PushNotification
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PushNotificationRepository : JpaRepository<PushNotification, UUID> {
    fun existsByUser_IdAndTypeAndReferenceKey(userId: UUID?, type: PushNotification.NotificationType?, referenceKey: String?): Boolean
    fun findAllByUser_IdOrderBySentAtDesc(userId: UUID?, pageable: Pageable?): List<PushNotification>
    fun countByUser_IdAndIsReadFalse(userId: UUID?): Long
}
