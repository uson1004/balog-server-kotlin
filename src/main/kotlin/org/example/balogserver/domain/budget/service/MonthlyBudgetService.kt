package org.example.balogserver.domain.budget.service

import org.example.balogserver.domain.budget.domain.repository.MonthlyBudgetRepository
import org.example.balogserver.domain.budget.presentation.dto.MonthlyBudgetResponse
import org.example.balogserver.domain.report.domain.repository.MonthlyReportRepository
import org.example.balogserver.domain.user.facade.UserFacade
import org.example.balogserver.global.error.exception.ErrorCode
import org.example.balogserver.global.error.exception.GlobalException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

@Service
class MonthlyBudgetService(
    private val userFacade: UserFacade,
    private val budgets: MonthlyBudgetRepository,
    private val reports: MonthlyReportRepository,
    private val clock: Clock,
) {
    @Transactional(readOnly = true)
    fun get(year: Int, month: Int): MonthlyBudgetResponse {
        val selectedMonth = validatedMonth(year, month)
        return summary(userFacade.currentUserId, selectedMonth)
    }

    @Transactional
    fun set(year: Int, month: Int, amount: Long): MonthlyBudgetResponse {
        val selectedMonth = validatedMonth(year, month)
        if (amount < 0) throw GlobalException(ErrorCode.INVALID_REQUEST)
        val userId = userFacade.currentUserId
        budgets.upsert(UUID.randomUUID(), userId, year, month, amount)
        return summary(userId, selectedMonth)
    }

    private fun summary(userId: UUID, month: YearMonth): MonthlyBudgetResponse {
        val previous = month.minusMonths(1)
        val expense = reports.findAmountSummary(
            userId, month.atDay(1), month.atEndOfMonth(), previous.atDay(1), previous.atEndOfMonth(),
        ).currentExpense ?: 0L
        return MonthlyBudgetResponse.of(
            month, budgets.findAmount(userId, month.year, month.monthValue), expense, LocalDate.now(clock),
        )
    }

    private fun validatedMonth(year: Int, month: Int): YearMonth {
        if (year !in 2000..2999 || month !in 1..12) throw GlobalException(ErrorCode.INVALID_REQUEST)
        return YearMonth.of(year, month)
    }
}
