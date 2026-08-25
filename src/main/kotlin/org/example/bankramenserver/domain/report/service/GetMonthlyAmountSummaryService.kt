package org.example.bankramenserver.domain.report.service

import org.example.bankramenserver.domain.report.domain.repository.MonthlyReportRepository
import org.example.bankramenserver.domain.report.presentation.dto.MonthlyAmountSummaryResponse
import org.example.bankramenserver.domain.user.facade.UserFacade
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.YearMonth
import java.util.UUID

@Service
class GetMonthlyAmountSummaryService(private val userFacade: UserFacade, private val monthlyReportRepository: MonthlyReportRepository) {
    @Transactional(readOnly = true)
    fun execute(year: Int, month: Int): MonthlyAmountSummaryResponse = execute(requireNotNull(userFacade.currentUser.id), year, month)

    @Transactional(readOnly = true)
    fun execute(userId: UUID, year: Int, month: Int): MonthlyAmountSummaryResponse {
        val currentMonth = YearMonth.of(year, month)
        val previousMonth = currentMonth.minusMonths(1)
        val amountSummary = monthlyReportRepository.findAmountSummary(userId, currentMonth.atDay(1), currentMonth.atEndOfMonth(), previousMonth.atDay(1), previousMonth.atEndOfMonth())
        return MonthlyAmountSummaryResponse.of(currentMonth, comparison(amountSummary.currentExpense(), amountSummary.previousExpense(), amountSummary.previousExpenseCount()), comparison(amountSummary.currentIncome(), amountSummary.previousIncome(), amountSummary.previousIncomeCount()))
    }

    private fun comparison(currentAmount: Long?, previousAmount: Long?, previousCount: Long?): MonthlyAmountSummaryResponse.AmountComparison {
        val current = currentAmount ?: 0L
        val previous = previousAmount ?: 0L
        return if (previousCount == null || previousCount <= 0L) MonthlyAmountSummaryResponse.AmountComparison.withoutPreviousMonth(current) else MonthlyAmountSummaryResponse.AmountComparison.of(current, previous, rate(current - previous, previous))
    }

    private fun rate(differenceAmount: Long, previousAmount: Long): BigDecimal? = if (previousAmount == 0L) null else BigDecimal.valueOf(differenceAmount).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(previousAmount), 1, RoundingMode.HALF_UP)
}
