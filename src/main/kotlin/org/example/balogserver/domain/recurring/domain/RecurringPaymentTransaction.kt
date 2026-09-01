package org.example.balogserver.domain.recurring.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.example.balogserver.domain.transaction.domain.Transaction
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "recurring_payment_transactions", uniqueConstraints = [UniqueConstraint(name = "uk_recurring_payment_transaction", columnNames = ["recurring_payment_id", "transaction_id"])])
class RecurringPaymentTransaction protected constructor() {
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.UUID)
    @field:Column(name = "recurring_payment_transaction_id")
    final var id: UUID? = null
        private set
    @field:ManyToOne(fetch = FetchType.LAZY)
    @field:JoinColumn(name = "recurring_payment_id", nullable = false)
    final lateinit var recurringPayment: RecurringPayment
        private set
    @field:ManyToOne(fetch = FetchType.LAZY)
    @field:JoinColumn(name = "transaction_id", nullable = false)
    final lateinit var transaction: Transaction
        private set
    @field:Enumerated(EnumType.STRING)
    @field:Column(nullable = false)
    final lateinit var matchType: MatchType
        private set
    @field:Column(nullable = false)
    final lateinit var matchedAt: LocalDateTime
        private set

    constructor(id: UUID?, recurringPayment: RecurringPayment, transaction: Transaction, matchType: MatchType, matchedAt: LocalDateTime) : this() {
        this.id = id
        this.recurringPayment = recurringPayment
        this.transaction = transaction
        this.matchType = matchType
        this.matchedAt = matchedAt
    }

    constructor(recurringPayment: RecurringPayment, transaction: Transaction, matchType: MatchType, matchedAt: LocalDateTime) : this(null, recurringPayment, transaction, matchType, matchedAt)

    companion object { @JvmStatic fun builder() = RecurringPaymentTransactionBuilder() }
    class RecurringPaymentTransactionBuilder {
        private var id: UUID? = null
        private var recurringPayment: RecurringPayment? = null
        private var transaction: Transaction? = null
        private var matchType: MatchType? = null
        private var matchedAt: LocalDateTime? = null
        fun id(value: UUID?) = apply { id = value }
        fun recurringPayment(value: RecurringPayment?) = apply { recurringPayment = value }
        fun transaction(value: Transaction?) = apply { transaction = value }
        fun matchType(value: MatchType?) = apply { matchType = value }
        fun matchedAt(value: LocalDateTime?) = apply { matchedAt = value }
        fun build() = RecurringPaymentTransaction(id, recurringPayment!!, transaction!!, matchType!!, matchedAt!!)
    }

    enum class MatchType { INITIAL, AUTO_DETECTED, MANUAL_ADDED, PAYMENT_CONFIRMED }
}
