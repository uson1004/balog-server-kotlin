package org.example.balogserver.domain.budget.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.util.UUID

@Entity
@Table(name = "monthly_budgets", uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "year", "month"])])
class MonthlyBudget protected constructor() {
    @field:Id
    @field:Column(columnDefinition = "BINARY(16)")
    final lateinit var id: UUID
        private set

    @field:Column(name = "user_id", nullable = false, columnDefinition = "BINARY(16)")
    final lateinit var userId: UUID
        private set

    @field:Column(nullable = false)
    final var year: Int = 0
        private set

    @field:Column(nullable = false)
    final var month: Int = 0
        private set

    @field:Column(nullable = false)
    final var amount: Long = 0
        private set
}
