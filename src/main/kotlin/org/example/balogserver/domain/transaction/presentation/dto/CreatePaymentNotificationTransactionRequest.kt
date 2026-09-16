package org.example.balogserver.domain.transaction.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.time.OffsetDateTime

@Schema(description = "결제 알림 거래 내역 추가 요청")
data class CreatePaymentNotificationTransactionRequest(
    @field:Schema(description = "클라이언트가 결제 알림에서 파싱한 결제 제목", example = "스타벅스 강남점")
    @field:NotBlank @field:Size(max = 255)
    val title: String,
    @field:Schema(description = "클라이언트가 결제 알림에서 파싱한 결제 금액", example = "4500")
    @field:NotNull @field:Positive
    val amount: Long,
    @field:Schema(description = "기기 로컬 큐에 영구 보관한 알림 수집 멱등성 키", example = "android:com.card:notification-key:1700000000000", nullable = true)
    @field:Size(max = 128)
    val idempotencyKey: String? = null,
    @field:Schema(description = "클라이언트 파서 버전", example = "android-notification-parser/2.1.0")
    @field:Size(max = 64)
    val parserVersion: String? = null,
    @field:Schema(description = "원문 없이 전달하는 알림 식별자", example = "com.card:tag:42:1700000000000", nullable = true)
    @field:Size(max = 128)
    val sourceNotificationId: String? = null,
    @field:Schema(description = "기기가 알림을 수집한 시각", example = "2026-08-12T14:30:00+09:00")
    val collectedAt: OffsetDateTime? = null,
    @field:Schema(description = "파싱된 결제 발생 시각. 없으면 수집 시각을 거래일로 사용합니다.", example = "2026-08-12T14:29:00+09:00", nullable = true)
    val occurredAt: OffsetDateTime? = null,
) {
    fun title() = title
    fun amount() = amount

    fun hasCollectionMetadata() = idempotencyKey != null && parserVersion != null && collectedAt != null
    fun hasPartialCollectionMetadata() = listOf(idempotencyKey, parserVersion, collectedAt).any { it != null } && !hasCollectionMetadata()
}
