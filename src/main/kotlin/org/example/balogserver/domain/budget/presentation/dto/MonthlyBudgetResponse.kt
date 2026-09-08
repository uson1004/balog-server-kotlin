package org.example.balogserver.domain.budget.presentation.dto

import java.time.LocalDate
import java.time.YearMonth

data class MonthlyBudgetResponse(
    val yearMonth: String,
    val budgetAmount: Long?,
    val totalExpense: Long,
    val remainingAmount: Long?,
    val remainingDays: Int,
    val dailyAvailableAmount: Long?,
) {
    companion object {
        fun of(month: YearMonth, budget: Long?, expense: Long, today: LocalDate): MonthlyBudgetResponse {
            val remainingDays = when {
                month < YearMonth.from(today) -> 0
                month > YearMonth.from(today) -> month.lengthOfMonth()
                else -> month.lengthOfMonth() - today.dayOfMonth + 1
            }
            val remaining = budget?.minus(expense)
            val daily = remaining?.let { if (remainingDays == 0) 0 else it.coerceAtLeast(0) / remainingDays }
            return MonthlyBudgetResponse(month.toString(), budget, expense, remaining, remainingDays, daily)
        }
    }
}
