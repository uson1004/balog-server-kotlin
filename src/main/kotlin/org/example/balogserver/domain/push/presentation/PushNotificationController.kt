package org.example.balogserver.domain.push.presentation

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.example.balogserver.domain.push.presentation.dto.PushNotificationListResponse
import org.example.balogserver.domain.push.service.GetPushNotificationListService
import org.example.balogserver.global.document.PushNotificationApiDocument
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping(value = ["/push-notifications"], produces = [MediaType.APPLICATION_JSON_VALUE])
class PushNotificationController(
    private val getPushNotificationListService: GetPushNotificationListService,
) : PushNotificationApiDocument {
    @GetMapping
    override fun getPushNotifications(@RequestParam(defaultValue = "20") @Min(1) @Max(50) limit: Int): PushNotificationListResponse =
        getPushNotificationListService.execute(limit)
}
