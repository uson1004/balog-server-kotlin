package org.example.bankramenserver.global.document;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.bankramenserver.domain.category.presentation.dto.CategoryListResponse;
import org.springframework.http.MediaType;

@Tag(name = "Category", description = "기본 제공 카테고리 조회 API")
public interface CategoryApiDocument {

    @Operation(
            summary = "기본 카테고리 목록 조회",
            description = "거래 등록 및 리포트 분류에 사용할 수 있는 기본 카테고리 코드를 조회합니다.",
            tags = {"Category"}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "기본 카테고리 목록 조회 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CategoryListResponse.class)
                    )
            )
    })
    CategoryListResponse getCategories();
}
