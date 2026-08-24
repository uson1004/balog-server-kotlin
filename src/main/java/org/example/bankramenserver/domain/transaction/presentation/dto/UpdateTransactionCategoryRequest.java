package org.example.bankramenserver.domain.transaction.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.example.bankramenserver.domain.category.domain.Category;

@Schema(description = "거래 내역 카테고리 변경 요청")
public record UpdateTransactionCategoryRequest(
        @Schema(description = "변경할 카테고리 enum 코드", example = "FOOD")
        @NotNull
        Category category
) {
}
