package org.example.bankramenserver.domain.push.domain;

import org.example.bankramenserver.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "push_notifications",
        indexes = {
                @Index(name = "idx_push_user_read", columnList = "user_id, is_read"),
                @Index(name = "idx_push_user_sent", columnList = "user_id, sent_at"),
                @Index(name = "idx_push_user_type_reference", columnList = "user_id, type, reference_key")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class PushNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "reference_key")
    private String referenceKey;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @CreatedDate
    @Column(name = "sent_at", nullable = false, updatable = false)
    private LocalDateTime sentAt;

    @Builder
    public PushNotification(User user, NotificationType type, String title, String body, String referenceKey) {
        this.user = user;
        this.type = type;
        this.title = title;
        this.body = body;
        this.referenceKey = referenceKey;
        this.isRead = false;
    }

    public void markAsRead() {
        this.isRead = true;
    }

    public enum NotificationType {
        PAYMENT_RECORDED,
        RECURRING_ALERT,
        PATTERN_DETECTED,
        MONTHLY_REPORT,

        RECURRING_CANDIDATE,
        RECURRING_PAYMENT_REMINDER,
        RECURRING_PAYMENT_CONFIRMED,
        RECURRING_PAYMENT_MISSING
    }
}
