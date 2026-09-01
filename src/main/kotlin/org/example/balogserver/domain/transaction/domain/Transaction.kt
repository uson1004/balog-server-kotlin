package org.example.balogserver.domain.transaction.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
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
import org.example.balogserver.domain.category.domain.Category
import org.example.balogserver.domain.push.domain.PushNotification
import org.example.balogserver.domain.user.domain.User
import org.example.balogserver.global.common.BaseEntity
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "transactions", indexes = [
    Index(name = "idx_transaction_user_type_date", columnList = "user_id, type, transaction_date"),
    Index(name = "idx_transaction_user_category", columnList = "user_id, category"),
    Index(name = "idx_transaction_user_date", columnList = "user_id, transaction_date"),
    Index(name = "idx_transaction_user_desc_amount_date", columnList = "user_id, description, amount, transaction_date"),
])
class Transaction protected constructor() : BaseEntity() {
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.UUID)
    @field:Column(columnDefinition = "BINARY(16)")
    final var id: UUID? = null
        private set
    @field:ManyToOne(fetch = FetchType.LAZY)
    @field:JoinColumn(name = "user_id", nullable = false)
    final lateinit var user: User
        private set
    @field:ManyToOne(fetch = FetchType.LAZY)
    @field:JoinColumn(name = "push_notification_id")
    final var pushNotification: PushNotification? = null
        private set
    @field:Enumerated(EnumType.STRING)
    @field:Column(name = "category", nullable = false)
    final lateinit var category: Category
        private set
    @field:Enumerated(EnumType.STRING)
    @field:Column(name = "type", nullable = false)
    final lateinit var type: TransactionType
        private set
    @field:Column(name = "amount", nullable = false)
    final var amount: Long? = null
        private set
    @field:Column(name = "description")
    final var description: String? = null
        private set
    @field:Enumerated(EnumType.STRING)
    @field:Column(name = "source", nullable = false)
    final lateinit var source: TransactionSource
        private set
    @field:Column(name = "raw_notification_text", columnDefinition = "TEXT")
    final var rawNotificationText: String? = null
        private set
    @field:Column(name = "transaction_date", nullable = false)
    final lateinit var transactionDate: LocalDate
        private set

    constructor(user: User, pushNotification: PushNotification?, category: Category, type: TransactionType, amount: Long?, description: String?, source: TransactionSource, rawNotificationText: String?, transactionDate: LocalDate) : this() {
        this.user = user
        this.pushNotification = pushNotification
        this.category = category
        this.type = type
        this.amount = amount
        this.description = description
        this.source = source
        this.rawNotificationText = rawNotificationText
        this.transactionDate = transactionDate
    }

    fun updateCategory(category: Category) { this.category = category }

    companion object { @JvmStatic fun builder() = TransactionBuilder() }
    class TransactionBuilder {
        private var user: User? = null
        private var pushNotification: PushNotification? = null
        private var category: Category? = null
        private var type: TransactionType? = null
        private var amount: Long? = null
        private var description: String? = null
        private var source: TransactionSource? = null
        private var rawNotificationText: String? = null
        private var transactionDate: LocalDate? = null
        fun user(value: User?) = apply { user = value }
        fun pushNotification(value: PushNotification?) = apply { pushNotification = value }
        fun category(value: Category?) = apply { category = value }
        fun type(value: TransactionType?) = apply { type = value }
        fun amount(value: Long?) = apply { amount = value }
        fun description(value: String?) = apply { description = value }
        fun source(value: TransactionSource?) = apply { source = value }
        fun rawNotificationText(value: String?) = apply { rawNotificationText = value }
        fun transactionDate(value: LocalDate?) = apply { transactionDate = value }
        fun build() = Transaction(user!!, pushNotification, category!!, type!!, amount, description, source!!, rawNotificationText, transactionDate!!)
    }

    enum class TransactionType { INCOME, EXPENSE }
    enum class TransactionSource { NOTIFICATION, MANUAL }
}
