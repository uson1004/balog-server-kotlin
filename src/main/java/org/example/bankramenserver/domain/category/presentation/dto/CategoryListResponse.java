package org.example.bankramenserver.domain.category.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "기본 카테고리 목록 응답")
public record CategoryListResponse(
        @Schema(description = "기본 제공 카테고리 목록")
        List<CategoryResponse> categories
) {
    public static CategoryListResponse from(List<CategoryResponse> categories) {
        return new CategoryListResponse(categories);
    }
}
