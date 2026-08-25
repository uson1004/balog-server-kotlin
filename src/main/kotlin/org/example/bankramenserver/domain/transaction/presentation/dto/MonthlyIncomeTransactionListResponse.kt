package org.example.bankramenserver.domain.transaction.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.YearMonth

@Schema(description = "월별 수입 내역 목록 응답")
data class MonthlyIncomeTransactionListResponse(
    @field:Schema(description = "조회 년월", example = "2026-08") val yearMonth: String,
    @field:Schema(description = "수입 거래 내역 목록") val incomes: List<TransactionHistoryResponse>,
) {
    fun yearMonth() = yearMonth
    fun incomes() = incomes

    companion object {
        @JvmStatic fun of(yearMonth: YearMonth, incomes: List<TransactionHistoryResponse>) = MonthlyIncomeTransactionListResponse(yearMonth.toString(), incomes)
    }
}
