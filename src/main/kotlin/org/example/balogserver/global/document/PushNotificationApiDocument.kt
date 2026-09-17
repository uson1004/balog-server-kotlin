package org.example.balogserver.global.document

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.example.balogserver.domain.push.presentation.dto.PushNotificationListResponse
import org.springframework.http.MediaType
import java.util.UUID

@Tag(name = "Push Notification", description = "푸시 알림 조회 API")
interface PushNotificationApiDocument {
    @Operation(summary = "푸시 알림 목록 조회", description = "Authorization 헤더의 액세스 토큰으로 현재 사용자를 식별하고, Firebase로 발송된 푸시 알림 저장 이력을 알림 화면 표시 정보와 함께 최신순으로 조회합니다.", tags = ["Push Notification"])
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(ApiResponse(responseCode = "200", description = "푸시 알림 목록 조회 성공", content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = PushNotificationListResponse::class))]), ApiResponse(responseCode = "400", description = "요청 파라미터 검증 실패", content = [Content()]), ApiResponse(responseCode = "401", description = "인증 실패", content = [Content()]))
    fun getPushNotifications(@Parameter(description = "조회할 최근 푸시 알림 개수", required = true, example = "20") @Min(1) @Max(50) limit: Int): PushNotificationListResponse

    @Operation(summary = "푸시 알림 읽음 처리", description = "현재 사용자의 푸시 알림을 읽음으로 처리합니다.", tags = ["Push Notification"])
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(ApiResponse(responseCode = "204", description = "푸시 알림 읽음 처리 성공", content = [Content()]), ApiResponse(responseCode = "401", description = "인증 실패", content = [Content()]), ApiResponse(responseCode = "404", description = "푸시 알림을 찾을 수 없음", content = [Content()]))
    fun markPushNotificationAsRead(@Parameter(description = "읽음 처리할 푸시 알림 ID", required = true) notificationId: UUID)
}
