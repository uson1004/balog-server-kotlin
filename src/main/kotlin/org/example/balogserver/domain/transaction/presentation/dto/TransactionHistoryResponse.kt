package org.example.balogserver.domain.transaction.presentation.dto

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import org.example.balogserver.domain.category.domain.Category
import org.example.balogserver.domain.transaction.domain.Transaction
import org.example.balogserver.domain.transaction.domain.repository.TransactionHistoryRow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

@Schema(description = "거래 내역 응답")
data class TransactionHistoryResponse(
    @field:Schema(description = "거래 내역 ID", example = "550e8400-e29b-41d4-a716-446655440000") val transactionId: UUID?,
    @field:Schema(description = "거래 제목 또는 설명", example = "스타벅스 강남점") val title: String?,
    @field:Schema(description = "거래 날짜", example = "2026-08-15") @field:JsonFormat(pattern = "yyyy-MM-dd") val transactionDate: LocalDate?,
    @field:Schema(description = "거래 시간. 별도 거래 시간이 없으면 생성 시간을 기준으로 응답합니다.", example = "14:30:00", nullable = true) @field:JsonFormat(pattern = "HH:mm:ss") val transactionTime: LocalTime?,
    @field:Schema(description = "거래 금액", example = "1400") val amount: Long,
    @field:Schema(description = "거래 유형", example = "EXPENSE") val type: Transaction.TransactionType?,
    @field:Schema(description = "카테고리 enum 코드", example = "FOOD") val category: Category?,
    @field:Schema(description = "사용자에게 표시할 카테고리명", example = "식비") val categoryName: String?,
) {
    fun transactionId() = transactionId
    fun title() = title
    fun transactionDate() = transactionDate
    fun transactionTime() = transactionTime
    fun amount() = amount
    fun type() = type
    fun category() = category
    fun categoryName() = categoryName

    companion object {
        @JvmStatic fun from(row: TransactionHistoryRow): TransactionHistoryResponse {
            val category = requireNotNull(row.category())
            return TransactionHistoryResponse(row.transactionId(), row.title(), row.transactionDate(), row.createdAt()?.toLocalTime(), row.amount() ?: 0L, row.type(), category, category.displayName)
        }

        @JvmStatic fun from(transaction: Transaction): TransactionHistoryResponse {
            val category = transaction.category
            return TransactionHistoryResponse(transaction.id, transaction.description, transaction.transactionDate, transaction.createdAt?.toLocalTime(), transaction.amount ?: 0L, transaction.type, category, category.displayName)
        }
    }
}
