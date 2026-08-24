package org.example.bankramenserver.domain.push.domain.repository;

import org.example.bankramenserver.domain.push.domain.PushNotification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PushNotificationRepository extends JpaRepository<PushNotification, UUID> {

    boolean existsByUser_IdAndTypeAndReferenceKey(
            UUID userId,
            PushNotification.NotificationType type,
            String referenceKey
    );

    List<PushNotification> findAllByUser_IdOrderBySentAtDesc(UUID userId, Pageable pageable);

    long countByUser_IdAndIsReadFalse(UUID userId);
}
