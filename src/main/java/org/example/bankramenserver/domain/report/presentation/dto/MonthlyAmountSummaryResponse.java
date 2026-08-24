package org.example.bankramenserver.domain.report.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.YearMonth;

@Schema(description = "월별 수입/지출 금액 요약 응답")
public record MonthlyAmountSummaryResponse(
        @Schema(description = "조회 년월", example = "2026-08")
        String yearMonth,
        @Schema(description = "지출 금액 비교 정보")
        AmountComparison expense,
        @Schema(description = "수입 금액 비교 정보")
        AmountComparison income
) {

    public static MonthlyAmountSummaryResponse of(
            YearMonth yearMonth,
            AmountComparison expense,
            AmountComparison income
    ) {
        return new MonthlyAmountSummaryResponse(yearMonth.toString(), expense, income);
    }

    @Schema(description = "현재 월과 지난달의 금액 비교 정보")
    public record AmountComparison(
            @Schema(description = "현재 월 총 금액", example = "1250000")
            long currentAmount,
            @Schema(description = "지난달 총 금액. 지난달 데이터가 없으면 null입니다.", example = "1500000", nullable = true)
            Long previousAmount,
            @Schema(description = "지난달 데이터 존재 여부", example = "true")
            boolean hasPreviousMonthData,
            @Schema(description = "지난달 대비 증감률. 지난달 데이터가 없거나 지난달 금액이 0원이면 null입니다.", example = "-17.00", nullable = true)
            BigDecimal differenceRate
    ) {

        public static AmountComparison withoutPreviousMonth(long currentAmount) {
            return new AmountComparison(
                    currentAmount,
                    null,
                    false,
                    null
            );
        }

        public static AmountComparison of(
                long currentAmount,
                long previousAmount,
                BigDecimal differenceRate
        ) {
            return new AmountComparison(
                    currentAmount,
                    previousAmount,
                    true,
                    differenceRate
            );
        }
    }
}
