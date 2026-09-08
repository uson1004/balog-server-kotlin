package org.example.balogserver.domain.budget.presentation.dto

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate
import java.time.YearMonth

data class MonthlyBudgetResponse(
    val yearMonth: String,
    val budgetAmount: Long?,
    val totalExpense: Long,
    val remainingAmount: Long?,
    val remainingDays: Int,
    val dailyAvailableAmount: Long?,
    val dailyTargetAmount: Long? = null,
    val days: List<DailyBudgetResponse> = emptyList(),
) {
    companion object {
        fun of(month: YearMonth, budget: Long?, expense: Long, today: LocalDate, expenses: Map<LocalDate, Long> = emptyMap()): MonthlyBudgetResponse {
            val remainingDays = when {
                month < YearMonth.from(today) -> 0
                month > YearMonth.from(today) -> month.lengthOfMonth()
                else -> month.lengthOfMonth() - today.dayOfMonth + 1
            }
            val remaining = budget?.minus(expense)
            val daily = remaining?.let { if (remainingDays == 0) 0 else it.coerceAtLeast(0) / remainingDays }
            val target = budget?.div(month.lengthOfMonth())
            val days = (1..month.lengthOfMonth()).map { day ->
                val date = month.atDay(day)
                val spent = expenses[date] ?: 0L
                val status = when {
                    target == null -> DailyBudgetStatus.UNSET
                    date >= today -> DailyBudgetStatus.PENDING
                    spent <= target -> DailyBudgetStatus.SUCCESS
                    else -> DailyBudgetStatus.FAIL
                }
                DailyBudgetResponse(date, target, spent, status)
            }
            return MonthlyBudgetResponse(month.toString(), budget, expense, remaining, remainingDays, daily, target, days)
        }
    }
}

data class DailyBudgetResponse(
    @field:JsonFormat(pattern = "yyyy-MM-dd") val date: LocalDate,
    val targetAmount: Long?,
    val expenseAmount: Long,
    val status: DailyBudgetStatus,
)

enum class DailyBudgetStatus { UNSET, PENDING, SUCCESS, FAIL }
