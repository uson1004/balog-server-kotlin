package org.example.bankramenserver.domain.transaction.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.YearMonth;
import java.util.List;

@Builder
@Schema(description = "월별 수입 내역 목록 응답")
public record MonthlyIncomeTransactionListResponse(
        @Schema(description = "조회 년월", example = "2026-08")
        String yearMonth,
        @Schema(description = "수입 거래 내역 목록")
        List<TransactionHistoryResponse> incomes
) {

    public static MonthlyIncomeTransactionListResponse of(
            YearMonth yearMonth,
            List<TransactionHistoryResponse> incomes
    ) {
        return MonthlyIncomeTransactionListResponse.builder()
                .yearMonth(yearMonth.toString())
                .incomes(incomes)
                .build();
    }
}
