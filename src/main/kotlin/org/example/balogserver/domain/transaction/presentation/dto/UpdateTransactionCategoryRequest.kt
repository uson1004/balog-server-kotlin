package org.example.balogserver.domain.transaction.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import org.example.balogserver.domain.category.domain.Category

@Schema(description = "거래 내역 카테고리 변경 요청")
data class UpdateTransactionCategoryRequest(
    @field:Schema(description = "변경할 카테고리 enum 코드", example = "FOOD")
    @field:NotNull val category: Category,
) {
    fun category() = category
}
