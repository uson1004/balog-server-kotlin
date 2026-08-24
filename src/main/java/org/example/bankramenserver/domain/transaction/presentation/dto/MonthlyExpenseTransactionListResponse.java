package org.example.bankramenserver.domain.transaction.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.YearMonth;
import java.util.List;

@Builder
@Schema(description = "월별 지출 내역 목록 응답")
public record MonthlyExpenseTransactionListResponse(
        @Schema(description = "조회 년월", example = "2026-08")
        String yearMonth,
        @Schema(description = "지출 거래 내역 목록")
        List<TransactionHistoryResponse> expenses
) {

    public static MonthlyExpenseTransactionListResponse of(
            YearMonth yearMonth,
            List<TransactionHistoryResponse> expenses
    ) {
        return MonthlyExpenseTransactionListResponse.builder()
                .yearMonth(yearMonth.toString())
                .expenses(expenses)
                .build();
    }
}
