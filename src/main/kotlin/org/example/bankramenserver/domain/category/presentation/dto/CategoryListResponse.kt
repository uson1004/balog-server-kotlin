package org.example.bankramenserver.domain.category.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "기본 카테고리 목록 응답")
data class CategoryListResponse(
    @Schema(description = "기본 제공 카테고리 목록") val categories: List<CategoryResponse>
) {
    fun categories() = categories

    companion object {
        @JvmStatic fun from(categories: List<CategoryResponse>) = CategoryListResponse(categories)
    }
}
