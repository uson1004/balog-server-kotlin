package org.example.balogserver.domain.push.presentation

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.example.balogserver.domain.push.presentation.dto.PushNotificationListResponse
import org.example.balogserver.domain.push.service.GetPushNotificationListService
import org.example.balogserver.domain.push.service.MarkPushNotificationAsReadService
import org.example.balogserver.global.document.PushNotificationApiDocument
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Validated
@RestController
@RequestMapping(value = ["/push-notifications"], produces = [MediaType.APPLICATION_JSON_VALUE])
class PushNotificationController(
    private val getPushNotificationListService: GetPushNotificationListService,
    private val markPushNotificationAsReadService: MarkPushNotificationAsReadService,
) : PushNotificationApiDocument {
    @GetMapping
    override fun getPushNotifications(@RequestParam(defaultValue = "20") @Min(1) @Max(50) limit: Int): PushNotificationListResponse =
        getPushNotificationListService.execute(limit)

    @PatchMapping("/{notificationId}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    override fun markPushNotificationAsRead(@PathVariable notificationId: UUID) =
        markPushNotificationAsReadService.execute(notificationId)
}
