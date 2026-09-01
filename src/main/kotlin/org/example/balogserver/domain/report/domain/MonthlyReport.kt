package org.example.balogserver.domain.report.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.example.balogserver.domain.user.domain.User
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "monthly_reports", uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "year", "month"])])
@EntityListeners(AuditingEntityListener::class)
class MonthlyReport protected constructor() {
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.UUID)
    @field:Column(columnDefinition = "BINARY(16)")
    final var id: UUID? = null
        private set
    @field:ManyToOne(fetch = FetchType.LAZY)
    @field:JoinColumn(name = "user_id", nullable = false)
    final lateinit var user: User
        private set
    @field:Column(name = "year", nullable = false)
    final var year: Int = 0
        private set
    @field:Column(name = "month", nullable = false)
    final var month: Int = 0
        private set
    @field:Column(name = "total_income", nullable = false)
    final var totalIncome: Long? = 0L
        private set
    @field:Column(name = "total_expense", nullable = false)
    final var totalExpense: Long? = 0L
        private set
    @field:Column(name = "prev_month_expense", nullable = false)
    final var prevMonthExpense: Long? = 0L
        private set
    @field:Column(name = "category_breakdown", columnDefinition = "JSON")
    final var categoryBreakdown: String? = null
        private set
    @field:CreatedDate
    @field:Column(name = "generated_at", nullable = false, updatable = false)
    final var generatedAt: LocalDateTime? = null
        private set

    constructor(user: User, year: Int, month: Int, totalIncome: Long?, totalExpense: Long?, prevMonthExpense: Long?, categoryBreakdown: String?) : this() {
        this.user = user
        this.year = year
        this.month = month
        this.totalIncome = totalIncome
        this.totalExpense = totalExpense
        this.prevMonthExpense = prevMonthExpense
        this.categoryBreakdown = categoryBreakdown
    }

    fun update(totalIncome: Long?, totalExpense: Long?, categoryBreakdown: String?) {
        this.totalIncome = totalIncome
        this.totalExpense = totalExpense
        this.categoryBreakdown = categoryBreakdown
    }

    companion object { @JvmStatic fun builder() = MonthlyReportBuilder() }
    class MonthlyReportBuilder {
        private var user: User? = null
        private var year = 0
        private var month = 0
        private var totalIncome: Long? = null
        private var totalExpense: Long? = null
        private var prevMonthExpense: Long? = null
        private var categoryBreakdown: String? = null
        fun user(value: User?) = apply { user = value }
        fun year(value: Int) = apply { year = value }
        fun month(value: Int) = apply { month = value }
        fun totalIncome(value: Long?) = apply { totalIncome = value }
        fun totalExpense(value: Long?) = apply { totalExpense = value }
        fun prevMonthExpense(value: Long?) = apply { prevMonthExpense = value }
        fun categoryBreakdown(value: String?) = apply { categoryBreakdown = value }
        fun build() = MonthlyReport(user!!, year, month, totalIncome, totalExpense, prevMonthExpense, categoryBreakdown)
    }
}
