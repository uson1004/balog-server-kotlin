package org.example.bankramenserver.domain.report.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.bankramenserver.domain.category.domain.Category;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

@Schema(description = "월별 카테고리별 지출 목록 응답")
public record MonthlyCategoryExpenseListResponse(
        @Schema(description = "조회 년월", example = "2026-08")
        String yearMonth,
        @Schema(description = "현재 월 전체 지출 금액", example = "1250000")
        long totalExpense,
        @Schema(description = "카테고리별 지출 목록")
        List<CategoryExpense> categories
) {

    public static MonthlyCategoryExpenseListResponse of(
            YearMonth yearMonth,
            long totalExpense,
            List<CategoryExpense> categories
    ) {
        return new MonthlyCategoryExpenseListResponse(yearMonth.toString(), totalExpense, categories);
    }

    @Schema(description = "카테고리별 지출 정보")
    public record CategoryExpense(
            @Schema(description = "카테고리 enum 코드", example = "FOOD")
            Category category,
            @Schema(description = "사용자에게 표시할 카테고리명", example = "식비")
            String categoryName,
            @Schema(description = "해당 카테고리의 현재 월 지출 금액", example = "450000")
            long expenseAmount,
            @Schema(description = "전체 지출 대비 해당 카테고리 지출 비율", example = "35.00")
            BigDecimal expenseRatio,
            @Schema(description = "지난달보다 해당 카테고리에 더 많이 지출했는지 여부", example = "true")
            boolean spentMoreThanPreviousMonth
    ) {

        public static CategoryExpense of(
                Category category,
                String categoryName,
                long expenseAmount,
                BigDecimal expenseRatio,
                boolean spentMoreThanPreviousMonth
        ) {
            return new CategoryExpense(
                    category,
                    categoryName,
                    expenseAmount,
                    expenseRatio,
                    spentMoreThanPreviousMonth
            );
        }
    }
}
