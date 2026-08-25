package org.example.bankramenserver.domain.transaction.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.YearMonth

@Schema(description = "월별 지출 내역 목록 응답")
data class MonthlyExpenseTransactionListResponse(
    @field:Schema(description = "조회 년월", example = "2026-08") val yearMonth: String,
    @field:Schema(description = "지출 거래 내역 목록") val expenses: List<TransactionHistoryResponse>,
) {
    fun yearMonth() = yearMonth
    fun expenses() = expenses

    companion object {
        @JvmStatic fun of(yearMonth: YearMonth, expenses: List<TransactionHistoryResponse>) = MonthlyExpenseTransactionListResponse(yearMonth.toString(), expenses)
    }
}
