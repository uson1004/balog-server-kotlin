package org.example.bankramenserver.global.document;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.example.bankramenserver.domain.transaction.presentation.dto.CreatePaymentNotificationTransactionRequest;
import org.example.bankramenserver.domain.transaction.presentation.dto.CreateTransactionRequest;
import org.example.bankramenserver.domain.transaction.presentation.dto.MonthlyExpenseTransactionListResponse;
import org.example.bankramenserver.domain.transaction.presentation.dto.MonthlyIncomeTransactionListResponse;
import org.example.bankramenserver.domain.transaction.presentation.dto.RecentTransactionListResponse;
import org.example.bankramenserver.domain.transaction.presentation.dto.TransactionHistoryResponse;
import org.example.bankramenserver.domain.transaction.presentation.dto.UpdateTransactionCategoryRequest;
import org.springframework.http.MediaType;

import java.util.UUID;

@Tag(name = "Transaction", description = "거래 내역 조회 및 관리 API")
public interface TransactionApiDocument {

    @Operation(
            summary = "최근 거래 내역 조회",
            description = "Authorization 헤더의 액세스 토큰으로 현재 사용자를 식별하고, 최신 거래 내역을 조회합니다.",
            tags = {"Transaction"}
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "최근 거래 내역 조회 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RecentTransactionListResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "요청 파라미터 검증 실패", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    })
    RecentTransactionListResponse getRecentTransactions(
            @Parameter(description = "조회할 최근 거래 내역 개수", required = true, example = "5")
            @Min(1) @Max(50) int limit
    );

    @Operation(
            summary = "거래 내역 추가",
            description = "Authorization 헤더의 액세스 토큰으로 현재 사용자를 식별하고, 수입 또는 지출 거래 내역을 직접 추가합니다.",
            tags = {"Transaction"}
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "거래 내역 추가 성공", content = @Content),
            @ApiResponse(responseCode = "400", description = "요청 본문 검증 실패", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    })
    void createTransaction(
            @RequestBody(
                    required = true,
                    description = "추가할 거래 내역 정보",
                    content = @Content(schema = @Schema(implementation = CreateTransactionRequest.class))
            )
            @Valid
            CreateTransactionRequest request
    );

    @Operation(
            summary = "결제 알림 거래 내역 추가",
            description = "클라이언트가 결제 알림에서 파싱한 제목과 금액을 전달하면, Gemini Flash 모델로 카테고리를 추천받아 지출 거래 내역을 추가합니다.",
            tags = {"Transaction"}
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "결제 알림 거래 내역 추가 성공", content = @Content),
            @ApiResponse(responseCode = "400", description = "요청 본문 검증 실패", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    })
    void createPaymentNotificationTransaction(
            @RequestBody(
                    required = true,
                    description = "결제 알림에서 파싱한 거래 정보",
                    content = @Content(schema = @Schema(implementation = CreatePaymentNotificationTransactionRequest.class))
            )
            @Valid
            CreatePaymentNotificationTransactionRequest request
    );

    @Operation(
            summary = "거래 내역 삭제",
            description = "Authorization 헤더의 액세스 토큰으로 현재 사용자를 식별하고, 본인의 거래 내역을 삭제합니다.",
            tags = {"Transaction"}
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "거래 내역 삭제 성공", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
            @ApiResponse(responseCode = "404", description = "거래 내역 없음", content = @Content)
    })
    void deleteTransaction(
            @Parameter(description = "거래 내역 ID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            UUID transactionId
    );

    @Operation(
            summary = "거래 내역 카테고리 변경",
            description = "Authorization 헤더의 액세스 토큰으로 현재 사용자를 식별하고, 본인의 거래 내역 카테고리를 변경합니다.",
            tags = {"Transaction"}
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "거래 내역 카테고리 변경 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TransactionHistoryResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "요청 본문 검증 실패", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
            @ApiResponse(responseCode = "404", description = "거래 내역 없음", content = @Content)
    })
    TransactionHistoryResponse updateTransactionCategory(
            @Parameter(description = "거래 내역 ID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            UUID transactionId,
            @RequestBody(
                    required = true,
                    description = "변경할 카테고리 정보",
                    content = @Content(schema = @Schema(implementation = UpdateTransactionCategoryRequest.class))
            )
            @Valid
            UpdateTransactionCategoryRequest request
    );

    @Operation(
            summary = "월별 수입 내역 조회",
            description = "Authorization 헤더의 액세스 토큰으로 현재 사용자를 식별하고, 선택한 월의 수입 거래 내역을 제목, 거래일, 거래시간, 금액, 카테고리 정보와 함께 조회합니다.",
            tags = {"Transaction"}
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "월별 수입 내역 조회 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MonthlyIncomeTransactionListResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "요청 파라미터 검증 실패", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    })
    MonthlyIncomeTransactionListResponse getMonthlyIncomeTransactions(
            @Parameter(description = "조회 연도", required = true, example = "2026")
            @Min(2000) @Max(2999) int year,
            @Parameter(description = "조회 월", required = true, example = "8")
            @Min(1) @Max(12) int month
    );

    @Operation(
            summary = "월별 지출 내역 조회",
            description = "Authorization 헤더의 액세스 토큰으로 현재 사용자를 식별하고, 선택한 월의 지출 거래 내역을 제목, 거래일, 거래시간, 금액, 카테고리 정보와 함께 조회합니다.",
            tags = {"Transaction"}
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "월별 지출 내역 조회 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MonthlyExpenseTransactionListResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "요청 파라미터 검증 실패", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    })
    MonthlyExpenseTransactionListResponse getMonthlyExpenseTransactions(
            @Parameter(description = "조회 연도", required = true, example = "2026")
            @Min(2000) @Max(2999) int year,
            @Parameter(description = "조회 월", required = true, example = "8")
            @Min(1) @Max(12) int month
    );
}
