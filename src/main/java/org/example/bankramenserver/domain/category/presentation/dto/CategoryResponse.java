package org.example.bankramenserver.domain.category.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.bankramenserver.domain.category.domain.Category;

@Schema(description = "기본 카테고리 응답")
public record CategoryResponse(
        @Schema(description = "카테고리 enum 코드", example = "FOOD")
        String code,
        @Schema(description = "사용자에게 표시할 카테고리명", example = "식비")
        String displayName
) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.name(),
                category.getDisplayName()
        );
    }
}
