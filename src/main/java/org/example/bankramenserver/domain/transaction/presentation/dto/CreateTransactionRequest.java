package org.example.bankramenserver.domain.transaction.presentation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.example.bankramenserver.domain.category.domain.Category;
import org.example.bankramenserver.domain.transaction.domain.Transaction;

import java.time.LocalDate;

@Schema(description = "거래 내역 추가 요청")
public record CreateTransactionRequest(
        @Schema(description = "거래 유형", example = "EXPENSE")
        @NotNull
        Transaction.TransactionType type,

        @Schema(description = "거래 금액", example = "4500")
        @NotNull @Positive
        Long amount,

        @Schema(description = "거래 제목 또는 사용처", example = "스타벅스 강남점")
        @NotBlank
        String title,

        @Schema(description = "카테고리 enum 코드", example = "FOOD")
        @NotNull
        Category category,

        @Schema(description = "거래 날짜", example = "2026-08-15")
        @NotNull
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate transactionDate
) {
}
