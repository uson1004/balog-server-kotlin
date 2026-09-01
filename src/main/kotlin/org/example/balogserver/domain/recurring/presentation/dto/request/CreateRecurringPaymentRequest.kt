package org.example.balogserver.domain.recurring.presentation.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import org.example.balogserver.domain.recurring.domain.RecurringPayment
import java.time.LocalDate
import java.util.UUID

@Schema(description = "정기결제 직접 등록 요청")
data class CreateRecurringPaymentRequest(
    @field:Schema(description = "정기결제로 등록할 거래 ID") @field:NotNull val transactionId: UUID?,
    @field:Schema(description = "정기결제 주기", example = "MONTHLY") @field:NotNull val cycle: RecurringPayment.Cycle?,
    @field:Schema(description = "다음 결제 예정일", example = "2026-06-15") @field:NotNull val nextBillingDate: LocalDate?,
)
