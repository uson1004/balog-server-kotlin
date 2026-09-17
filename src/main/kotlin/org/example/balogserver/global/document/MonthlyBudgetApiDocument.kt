package org.example.balogserver.global.document

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.example.balogserver.domain.budget.presentation.dto.MonthlyBudgetResponse
import org.example.balogserver.domain.budget.presentation.dto.SetMonthlyBudgetRequest

@Tag(name = "Monthly Budget", description = "현재 사용자의 월 예산과 하루 사용 가능액")
@SecurityRequirement(name = "bearerAuth")
@ApiResponses(
    ApiResponse(responseCode = "200", description = "월 예산 요약 반환"),
    ApiResponse(responseCode = "400", description = "연도 2000~2999, 월 1~12, 금액 0 이상의 정수 필요"),
    ApiResponse(responseCode = "401", description = "유효한 access JWT 필요"),
)
interface MonthlyBudgetApiDocument {
    @Operation(summary = "월 예산 조회", description = "날짜별 고정 목표와 지출, 달성 상태를 함께 반환합니다. 오늘과 미래는 PENDING, 지난 날짜는 목표 이하 SUCCESS/초과 FAIL, 예산 미설정은 UNSET입니다. 등록된 월 지출 전체를 집계합니다. 미설정 예산은 null, 남은 일수는 오늘 포함입니다. 과거 월의 하루 사용 가능액은 0입니다.")
    fun get(year: Int, month: Int): MonthlyBudgetResponse

    @Operation(summary = "월 예산 설정 또는 수정", description = "금액 단위는 원입니다. 같은 사용자와 월의 예산을 교체하며 반복 요청은 중복 예산을 만들지 않습니다.")
    fun set(year: Int, month: Int, request: SetMonthlyBudgetRequest): MonthlyBudgetResponse
}
