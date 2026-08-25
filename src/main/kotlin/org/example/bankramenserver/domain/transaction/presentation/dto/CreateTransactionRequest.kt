package org.example.bankramenserver.domain.transaction.presentation.dto

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import org.example.bankramenserver.domain.category.domain.Category
import org.example.bankramenserver.domain.transaction.domain.Transaction
import java.time.LocalDate

@Schema(description = "거래 내역 추가 요청")
data class CreateTransactionRequest(
    @field:Schema(description = "거래 유형", example = "EXPENSE")
    @field:NotNull val type: Transaction.TransactionType,
    @field:Schema(description = "거래 금액", example = "4500")
    @field:NotNull @field:Positive val amount: Long,
    @field:Schema(description = "거래 제목 또는 사용처", example = "스타벅스 강남점")
    @field:NotBlank val title: String,
    @field:Schema(description = "카테고리 enum 코드", example = "FOOD")
    @field:NotNull val category: Category,
    @field:Schema(description = "거래 날짜", example = "2026-08-15")
    @field:NotNull @field:JsonFormat(pattern = "yyyy-MM-dd") val transactionDate: LocalDate,
) {
    fun type() = type
    fun amount() = amount
    fun title() = title
    fun category() = category
    fun transactionDate() = transactionDate
}
