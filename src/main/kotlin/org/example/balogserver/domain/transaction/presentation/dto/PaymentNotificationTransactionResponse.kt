package org.example.balogserver.domain.transaction.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

@Schema(description = "결제 알림 거래 저장 결과")
data class PaymentNotificationTransactionResponse(
    @field:Schema(description = "저장된 거래 ID") val transactionId: UUID?,
    @field:Schema(description = "이번 요청으로 새 거래가 생성됐는지 여부") val created: Boolean,
)
