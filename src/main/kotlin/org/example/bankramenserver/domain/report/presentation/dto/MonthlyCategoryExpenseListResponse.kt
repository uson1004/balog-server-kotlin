package org.example.bankramenserver.domain.report.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema
import org.example.bankramenserver.domain.category.domain.Category
import java.math.BigDecimal
import java.time.YearMonth

@Schema(description = "월별 카테고리별 지출 목록 응답")
data class MonthlyCategoryExpenseListResponse(
    @field:Schema(description = "조회 년월", example = "2026-08") val yearMonth: String,
    @field:Schema(description = "현재 월 전체 지출 금액", example = "1250000") val totalExpense: Long,
    @field:Schema(description = "카테고리별 지출 목록") val categories: List<CategoryExpense>,
) {
    fun yearMonth() = yearMonth
    fun totalExpense() = totalExpense
    fun categories() = categories

    companion object {
        @JvmStatic fun of(yearMonth: YearMonth, totalExpense: Long, categories: List<CategoryExpense>) = MonthlyCategoryExpenseListResponse(yearMonth.toString(), totalExpense, categories)
    }

    @Schema(description = "카테고리별 지출 정보")
    data class CategoryExpense(
        @field:Schema(description = "카테고리 enum 코드", example = "FOOD") val category: Category,
        @field:Schema(description = "사용자에게 표시할 카테고리명", example = "식비") val categoryName: String,
        @field:Schema(description = "해당 카테고리의 현재 월 지출 금액", example = "450000") val expenseAmount: Long,
        @field:Schema(description = "전체 지출 대비 해당 카테고리 지출 비율", example = "35.00") val expenseRatio: BigDecimal,
        @field:Schema(description = "지난달보다 해당 카테고리에 더 많이 지출했는지 여부", example = "true") val spentMoreThanPreviousMonth: Boolean,
    ) {
        fun category() = category
        fun categoryName() = categoryName
        fun expenseAmount() = expenseAmount
        fun expenseRatio() = expenseRatio
        fun spentMoreThanPreviousMonth() = spentMoreThanPreviousMonth

        companion object {
            @JvmStatic fun of(category: Category, categoryName: String, expenseAmount: Long, expenseRatio: BigDecimal, spentMoreThanPreviousMonth: Boolean) = CategoryExpense(category, categoryName, expenseAmount, expenseRatio, spentMoreThanPreviousMonth)
        }
    }
}
