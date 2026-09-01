package org.example.balogserver.domain.report.service

import org.example.balogserver.domain.report.domain.repository.CategoryExpenseRow
import org.example.balogserver.domain.report.domain.repository.MonthlyReportRepository
import org.example.balogserver.domain.report.presentation.dto.MonthlyCategoryExpenseListResponse
import org.example.balogserver.domain.user.facade.UserFacade
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.YearMonth
import java.util.UUID

@Service
class GetMonthlyCategoryExpenseListService(private val userFacade: UserFacade, private val monthlyReportRepository: MonthlyReportRepository) {
    @Transactional(readOnly = true)
    fun execute(year: Int, month: Int): MonthlyCategoryExpenseListResponse = execute(requireNotNull(userFacade.currentUser.id), year, month)

    @Transactional(readOnly = true)
    fun execute(userId: UUID, year: Int, month: Int): MonthlyCategoryExpenseListResponse {
        val currentMonth = YearMonth.of(year, month)
        val previousMonth = currentMonth.minusMonths(1)
        val rows = monthlyReportRepository.findCategoryExpenseComparisons(userId, currentMonth.atDay(1), currentMonth.atEndOfMonth(), previousMonth.atDay(1), previousMonth.atEndOfMonth())
        val totalExpense = rows.sumOf { it.currentExpense() ?: 0L }
        return MonthlyCategoryExpenseListResponse.of(currentMonth, totalExpense, rows.map { categoryExpense(it, totalExpense) })
    }

    private fun categoryExpense(row: CategoryExpenseRow, totalExpense: Long): MonthlyCategoryExpenseListResponse.CategoryExpense {
        val expenseAmount = row.currentExpense() ?: 0L
        val category = requireNotNull(row.category())
        return MonthlyCategoryExpenseListResponse.CategoryExpense.of(category, category.displayName, expenseAmount, ratio(expenseAmount, totalExpense), expenseAmount > (row.previousExpense() ?: 0L))
    }

    private fun ratio(amount: Long, totalExpense: Long): BigDecimal = if (totalExpense <= 0L) BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP) else BigDecimal.valueOf(amount).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(totalExpense), 1, RoundingMode.HALF_UP)
}
