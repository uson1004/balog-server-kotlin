package org.example.bankramenserver.domain.push.presentation;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.example.bankramenserver.domain.push.presentation.dto.PushNotificationListResponse;
import org.example.bankramenserver.domain.push.service.GetPushNotificationListService;
import org.example.bankramenserver.global.document.PushNotificationApiDocument;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/push-notifications", produces = MediaType.APPLICATION_JSON_VALUE)
public class PushNotificationController implements PushNotificationApiDocument {

    private final GetPushNotificationListService getPushNotificationListService;

    @Override
    @GetMapping
    public PushNotificationListResponse getPushNotifications(
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int limit
    ) {
        return getPushNotificationListService.execute(limit);
    }
}
