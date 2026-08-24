package org.example.bankramenserver.domain.transaction.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(description = "최근 거래 내역 목록 응답")
public record RecentTransactionListResponse(
        @Schema(description = "최근 거래 내역 목록")
        List<TransactionHistoryResponse> transactions
) {

    public static RecentTransactionListResponse from(List<TransactionHistoryResponse> transactions) {
        return RecentTransactionListResponse.builder()
                .transactions(transactions)
                .build();
    }
}
