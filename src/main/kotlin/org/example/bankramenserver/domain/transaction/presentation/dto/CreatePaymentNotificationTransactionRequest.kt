package org.example.bankramenserver.domain.transaction.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

@Schema(description = "결제 알림 거래 내역 추가 요청")
data class CreatePaymentNotificationTransactionRequest(
    @field:Schema(description = "클라이언트가 결제 알림에서 파싱한 결제 제목", example = "스타벅스 강남점")
    @field:NotBlank
    val title: String,
    @field:Schema(description = "클라이언트가 결제 알림에서 파싱한 결제 금액", example = "4500")
    @field:NotNull @field:Positive
    val amount: Long,
) {
    fun title() = title
    fun amount() = amount
}
