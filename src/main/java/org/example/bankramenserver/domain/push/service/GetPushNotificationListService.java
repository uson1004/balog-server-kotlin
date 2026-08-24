package org.example.bankramenserver.domain.push.service;

import lombok.RequiredArgsConstructor;
import org.example.bankramenserver.domain.push.domain.PushNotification;
import org.example.bankramenserver.domain.push.domain.repository.PushNotificationRepository;
import org.example.bankramenserver.domain.push.presentation.dto.PushNotificationListResponse;
import org.example.bankramenserver.domain.user.facade.UserFacade;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetPushNotificationListService {

    private final UserFacade userFacade;
    private final PushNotificationRepository pushNotificationRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public PushNotificationListResponse execute(int limit) {
        UUID currentUserId = userFacade.getCurrentUserId();
        List<PushNotification> pushNotifications = pushNotificationRepository.findAllByUser_IdOrderBySentAtDesc(
                currentUserId,
                PageRequest.of(0, limit)
        );
        long unreadCount = pushNotificationRepository.countByUser_IdAndIsReadFalse(currentUserId);

        return PushNotificationListResponse.from(pushNotifications, unreadCount, clock);
    }
}
