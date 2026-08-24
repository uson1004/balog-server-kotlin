package org.example.bankramenserver.global.document;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.example.bankramenserver.domain.report.presentation.dto.MonthlyAmountSummaryResponse;
import org.example.bankramenserver.domain.report.presentation.dto.MonthlyCategoryExpenseListResponse;
import org.springframework.http.MediaType;

@Tag(name = "Monthly Report", description = "월별 리포트 조회 API")
public interface MonthlyReportApiDocument {

    @Operation(
            summary = "월별 금액 요약 조회",
            description = "Authorization 헤더의 액세스 토큰으로 현재 사용자를 식별하고, 선택한 월의 총 지출과 총 수입을 지난달 금액 및 증감률과 함께 조회합니다.",
            tags = {"Monthly Report"}
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "월별 금액 요약 조회 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MonthlyAmountSummaryResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "요청 파라미터 검증 실패", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    })
    MonthlyAmountSummaryResponse getMonthlyAmountSummary(
            @Parameter(description = "조회 연도", required = true, example = "2026")
            @Min(2000) @Max(2999) int year,
            @Parameter(description = "조회 월", required = true, example = "8")
            @Min(1) @Max(12) int month
    );

    @Operation(
            summary = "월별 카테고리 지출 목록 조회",
            description = "Authorization 헤더의 액세스 토큰으로 현재 사용자를 식별하고, 선택한 월의 카테고리별 지출 금액, 전체 지출 대비 비율, 지난달보다 더 많이 사용했는지 여부를 조회합니다.",
            tags = {"Monthly Report"}
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "월별 카테고리 지출 목록 조회 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MonthlyCategoryExpenseListResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "요청 파라미터 검증 실패", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    })
    MonthlyCategoryExpenseListResponse getMonthlyCategoryExpenses(
            @Parameter(description = "조회 연도", required = true, example = "2026")
            @Min(2000) @Max(2999) int year,
            @Parameter(description = "조회 월", required = true, example = "8")
            @Min(1) @Max(12) int month
    );
}
