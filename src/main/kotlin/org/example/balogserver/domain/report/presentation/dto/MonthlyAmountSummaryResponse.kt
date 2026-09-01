package org.example.balogserver.domain.report.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.YearMonth

@Schema(description = "월별 수입/지출 금액 요약 응답")
data class MonthlyAmountSummaryResponse(
    @field:Schema(description = "조회 년월", example = "2026-08") val yearMonth: String,
    @field:Schema(description = "지출 금액 비교 정보") val expense: AmountComparison,
    @field:Schema(description = "수입 금액 비교 정보") val income: AmountComparison,
) {
    fun yearMonth() = yearMonth
    fun expense() = expense
    fun income() = income

    companion object {
        @JvmStatic fun of(yearMonth: YearMonth, expense: AmountComparison, income: AmountComparison) = MonthlyAmountSummaryResponse(yearMonth.toString(), expense, income)
    }

    @Schema(description = "현재 월과 지난달의 금액 비교 정보")
    data class AmountComparison(
        @field:Schema(description = "현재 월 총 금액", example = "1250000") val currentAmount: Long,
        @field:Schema(description = "지난달 총 금액. 지난달 데이터가 없으면 null입니다.", example = "1500000", nullable = true) val previousAmount: Long?,
        @field:Schema(description = "지난달 데이터 존재 여부", example = "true") val hasPreviousMonthData: Boolean,
        @field:Schema(description = "지난달 대비 증감률. 지난달 데이터가 없거나 지난달 금액이 0원이면 null입니다.", example = "-17.00", nullable = true) val differenceRate: BigDecimal?,
    ) {
        fun currentAmount() = currentAmount
        fun previousAmount() = previousAmount
        fun hasPreviousMonthData() = hasPreviousMonthData
        fun differenceRate() = differenceRate

        companion object {
            @JvmStatic fun withoutPreviousMonth(currentAmount: Long) = AmountComparison(currentAmount, null, false, null)
            @JvmStatic fun of(currentAmount: Long, previousAmount: Long, differenceRate: BigDecimal?) = AmountComparison(currentAmount, previousAmount, true, differenceRate)
        }
    }
}
