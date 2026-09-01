package org.example.balogserver.domain.report.domain.repository

import java.time.LocalDate
import java.util.UUID

interface MonthlyReportRepositoryCustom {
    fun findAmountSummary(userId: UUID?, currentStartDate: LocalDate?, currentEndDate: LocalDate?, previousStartDate: LocalDate?, previousEndDate: LocalDate?): AmountSummaryRow
    fun findCategoryExpenseComparisons(userId: UUID?, currentStartDate: LocalDate?, currentEndDate: LocalDate?, previousStartDate: LocalDate?, previousEndDate: LocalDate?): List<CategoryExpenseRow>
}
