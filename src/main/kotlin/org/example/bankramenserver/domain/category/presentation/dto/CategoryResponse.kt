package org.example.bankramenserver.domain.category.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema
import org.example.bankramenserver.domain.category.domain.Category

@Schema(description = "기본 카테고리 응답")
data class CategoryResponse(
    @Schema(description = "카테고리 enum 코드", example = "FOOD") val code: String,
    @Schema(description = "사용자에게 표시할 카테고리명", example = "식비") val displayName: String
) {
    fun code() = code
    fun displayName() = displayName

    companion object {
        @JvmStatic fun from(category: Category) = CategoryResponse(category.name, category.displayName)
    }
}
