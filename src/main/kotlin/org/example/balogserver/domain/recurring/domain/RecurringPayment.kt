package org.example.balogserver.domain.recurring.domain

import jakarta.persistence.CascadeType
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
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.example.balogserver.domain.category.domain.Category
import org.example.balogserver.domain.transaction.domain.Transaction
import org.example.balogserver.domain.user.domain.User
import java.time.LocalDateTime
import java.time.YearMonth
import java.util.UUID

@Entity
@Table(name = "recurring_payments")
class RecurringPayment protected constructor() {
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.UUID)
    @field:Column(name = "id")
    final var id: UUID? = null
        private set
    @field:ManyToOne(fetch = FetchType.LAZY)
    @field:JoinColumn(name = "user_id", nullable = false)
    final lateinit var user: User
        private set
    @field:Enumerated(EnumType.STRING)
    @field:Column(nullable = false)
    final lateinit var category: Category
        private set
    @field:Column(nullable = false)
    final lateinit var name: String
        private set
    @field:Column(nullable = false)
    final var amount: Long? = null
        private set
    @field:Enumerated(EnumType.STRING)
    @field:Column(nullable = false)
    final lateinit var cycle: Cycle
        private set
    @field:Column(nullable = false)
    final var billingDay = 0
        private set
    @field:Column(nullable = false)
    final lateinit var nextBillingDate: LocalDateTime
        private set
    @field:Enumerated(EnumType.STRING)
    @field:Column(nullable = false)
    final lateinit var registrationType: RegistrationType
        private set
    @field:Column(nullable = false)
    final var confirmed = false
        private set
    @field:Column(nullable = false)
    final var active = true
        private set
    @field:OneToMany(mappedBy = "recurringPayment", cascade = [CascadeType.ALL], orphanRemoval = true)
    final var transactions: MutableList<RecurringPaymentTransaction> = ArrayList()
        private set

    constructor(id: UUID?, user: User, category: Category, name: String, amount: Long?, cycle: Cycle, billingDay: Int, nextBillingDate: LocalDateTime, registrationType: RegistrationType, confirmed: Boolean, active: Boolean, transactions: MutableList<RecurringPaymentTransaction>?) : this() {
        this.id = id
        this.user = user
        this.category = category
        this.name = name
        this.amount = amount
        this.cycle = cycle
        this.billingDay = billingDay
        this.nextBillingDate = nextBillingDate
        this.registrationType = registrationType
        this.confirmed = confirmed
        this.active = active
        this.transactions = transactions ?: ArrayList()
    }

    fun confirm() { confirmed = true }
    fun deactivate() { active = false }
    fun updateAfterPaymentDetected(nextBillingDate: LocalDateTime) { this.nextBillingDate = nextBillingDate }
    val isConfirmed get() = confirmed
    val isActive get() = active

    fun calculateNextBillingDate(): LocalDateTime {
        val base = if (cycle == Cycle.MONTHLY) nextBillingDate.plusMonths(1) else nextBillingDate.plusYears(1)
        val actualBillingDay = minOf(billingDay, YearMonth.from(base).lengthOfMonth())
        return base.withDayOfMonth(actualBillingDay)
            .withHour(nextBillingDate.hour)
            .withMinute(nextBillingDate.minute)
            .withSecond(nextBillingDate.second)
            .withNano(nextBillingDate.nano)
    }

    fun addTransaction(transaction: Transaction, matchType: RecurringPaymentTransaction.MatchType, matchedAt: LocalDateTime) {
        if (transactions.any { it.transaction.id!!.equals(transaction.id) }) return
        transactions.add(RecurringPaymentTransaction(this, transaction, matchType, matchedAt))
    }

    companion object { @JvmStatic fun builder() = RecurringPaymentBuilder() }
    class RecurringPaymentBuilder {
        private var id: UUID? = null
        private var user: User? = null
        private var category: Category? = null
        private var name: String? = null
        private var amount: Long? = null
        private var cycle: Cycle? = null
        private var billingDay = 0
        private var nextBillingDate: LocalDateTime? = null
        private var registrationType: RegistrationType? = null
        private var confirmed = false
        private var active = true
        private var transactions: MutableList<RecurringPaymentTransaction>? = null
        fun id(value: UUID?) = apply { id = value }
        fun user(value: User?) = apply { user = value }
        fun category(value: Category?) = apply { category = value }
        fun name(value: String?) = apply { name = value }
        fun amount(value: Long?) = apply { amount = value }
        fun cycle(value: Cycle?) = apply { cycle = value }
        fun billingDay(value: Int) = apply { billingDay = value }
        fun nextBillingDate(value: LocalDateTime?) = apply { nextBillingDate = value }
        fun registrationType(value: RegistrationType?) = apply { registrationType = value }
        fun confirmed(value: Boolean) = apply { confirmed = value }
        fun active(value: Boolean) = apply { active = value }
        fun transactions(value: MutableList<RecurringPaymentTransaction>?) = apply { transactions = value }
        fun build() = RecurringPayment(id, user!!, category!!, name!!, amount, cycle!!, billingDay, nextBillingDate!!, registrationType!!, confirmed, active, transactions)
    }

    enum class Cycle { MONTHLY, YEARLY }
    enum class RegistrationType { MANUAL, AUTO_DETECTED }
}
