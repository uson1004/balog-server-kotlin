package org.example.balogserver.domain.budget.service

import org.example.balogserver.domain.budget.domain.repository.MonthlyBudgetRepository
import org.example.balogserver.domain.budget.presentation.dto.MonthlyBudgetResponse
import org.example.balogserver.domain.transaction.domain.repository.TransactionRepository
import org.example.balogserver.domain.transaction.domain.Transaction
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
    private val transactions: TransactionRepository,
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
        return try {
            val expenses = transactions.findTransactionHistories(
                userId, Transaction.TransactionType.EXPENSE, month.atDay(1), month.atEndOfMonth(),
            ).groupBy { requireNotNull(it.transactionDate) }
                .mapValues { (_, rows) -> rows.fold(0L) { total, row -> Math.addExact(total, row.amount ?: 0L) } }
            MonthlyBudgetResponse.of(
                month, budgets.findAmount(userId, month.year, month.monthValue), expenses.values.fold(0L, Math::addExact), LocalDate.now(clock), expenses,
            )
        } catch (exception: ArithmeticException) {
            throw GlobalException(ErrorCode.INVALID_REQUEST, exception)
        }
    }

    private fun validatedMonth(year: Int, month: Int): YearMonth {
        if (year !in 2000..2999 || month !in 1..12) throw GlobalException(ErrorCode.INVALID_REQUEST)
        return YearMonth.of(year, month)
    }
}
