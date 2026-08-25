package org.example.bankramenserver.domain.transaction.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "최근 거래 내역 목록 응답")
data class RecentTransactionListResponse(
    @field:Schema(description = "최근 거래 내역 목록") val transactions: List<TransactionHistoryResponse>,
) {
    fun transactions() = transactions

    companion object {
        @JvmStatic fun from(transactions: List<TransactionHistoryResponse>) = RecentTransactionListResponse(transactions)
    }
}
