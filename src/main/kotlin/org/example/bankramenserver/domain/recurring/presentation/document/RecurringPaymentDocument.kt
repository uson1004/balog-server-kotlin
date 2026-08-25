package org.example.bankramenserver.domain.recurring.presentation.document

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.example.bankramenserver.domain.recurring.presentation.dto.request.CreateRecurringPaymentRequest
import org.example.bankramenserver.domain.recurring.presentation.dto.response.ConfirmRecurringPaymentResponse
import org.example.bankramenserver.domain.recurring.presentation.dto.response.CreateRecurringPaymentResponse
import org.example.bankramenserver.domain.recurring.presentation.dto.response.DeleteRecurringPaymentResponse
import org.example.bankramenserver.domain.recurring.presentation.dto.response.RecurringPaymentListResponse
import java.util.UUID

@Tag(name = "정기결제 API", description = "정기결제 등록, 목록 조회, 삭제, 자동 감지 후보 확정 API")
interface RecurringPaymentDocument {
    @Operation(summary = "정기결제 목록 조회", description = "사용자의 활성 정기결제 목록과 이번 달 예정된 정기결제 총 결제 금액을 조회합니다.")
    fun getRecurringPayments(): RecurringPaymentListResponse
    @Operation(summary = "정기결제 직접 등록", description = "사용자가 거래 기록을 기준으로 정기결제를 직접 등록합니다.")
    fun create(request: CreateRecurringPaymentRequest): CreateRecurringPaymentResponse
    @Operation(summary = "자동 감지 정기결제 후보 확정", description = "반복 결제로 자동 감지된 정기결제 후보를 사용자가 확인하여 확정 상태로 변경합니다.")
    fun confirm(@Parameter(description = "확정할 정기결제 ID") recurringPaymentId: UUID): ConfirmRecurringPaymentResponse
    @Operation(summary = "정기결제 삭제", description = "정기결제를 비활성화합니다.")
    fun delete(@Parameter(description = "삭제할 정기결제 ID") recurringPaymentId: UUID): DeleteRecurringPaymentResponse
}
