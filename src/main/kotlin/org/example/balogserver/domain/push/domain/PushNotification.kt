package org.example.balogserver.domain.push.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.example.balogserver.domain.user.domain.User
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "push_notifications", indexes = [
    Index(name = "idx_push_user_read", columnList = "user_id, is_read"),
    Index(name = "idx_push_user_sent", columnList = "user_id, sent_at"),
    Index(name = "idx_push_user_type_reference", columnList = "user_id, type, reference_key"),
])
@EntityListeners(AuditingEntityListener::class)
class PushNotification protected constructor() {
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.UUID)
    @field:Column(columnDefinition = "BINARY(16)")
    final var id: UUID? = null
        private set
    @field:ManyToOne(fetch = FetchType.LAZY)
    @field:JoinColumn(name = "user_id", nullable = false)
    final lateinit var user: User
        private set
    @field:Enumerated(EnumType.STRING)
    @field:Column(name = "type", nullable = false)
    final lateinit var type: NotificationType
        private set
    @field:Column(name = "title", nullable = false)
    final lateinit var title: String
        private set
    @field:Column(name = "body", nullable = false, columnDefinition = "TEXT")
    final lateinit var body: String
        private set
    @field:Column(name = "reference_key")
    final var referenceKey: String? = null
        private set
    @field:Column(name = "is_read", nullable = false)
    final var isRead: Boolean = false
        private set
    @field:CreatedDate
    @field:Column(name = "sent_at", nullable = false, updatable = false)
    final var sentAt: LocalDateTime? = null
        private set

    constructor(user: User, type: NotificationType, title: String, body: String, referenceKey: String?) : this() {
        this.user = user
        this.type = type
        this.title = title
        this.body = body
        this.referenceKey = referenceKey
        this.isRead = false
    }

    fun markAsRead() { isRead = true }

    companion object { @JvmStatic fun builder() = PushNotificationBuilder() }
    class PushNotificationBuilder {
        private var user: User? = null
        private var type: NotificationType? = null
        private var title: String? = null
        private var body: String? = null
        private var referenceKey: String? = null
        fun user(value: User?) = apply { user = value }
        fun type(value: NotificationType?) = apply { type = value }
        fun title(value: String?) = apply { title = value }
        fun body(value: String?) = apply { body = value }
        fun referenceKey(value: String?) = apply { referenceKey = value }
        fun build() = PushNotification(user!!, type!!, title!!, body!!, referenceKey)
    }

    enum class NotificationType {
        PAYMENT_RECORDED, RECURRING_ALERT, PATTERN_DETECTED, MONTHLY_REPORT,
        RECURRING_CANDIDATE, RECURRING_PAYMENT_REMINDER, RECURRING_PAYMENT_CONFIRMED, RECURRING_PAYMENT_MISSING,
    }
}
