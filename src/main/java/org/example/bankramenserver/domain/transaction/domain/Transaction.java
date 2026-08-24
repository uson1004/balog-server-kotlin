package org.example.bankramenserver.domain.transaction.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.bankramenserver.domain.category.domain.Category;
import org.example.bankramenserver.domain.push.domain.PushNotification;
import org.example.bankramenserver.domain.user.domain.User;
import org.example.bankramenserver.global.common.BaseEntity;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "transactions",
        indexes = {
                @Index(name = "idx_transaction_user_type_date", columnList = "user_id, type, transaction_date"),
                @Index(name = "idx_transaction_user_category", columnList = "user_id, category"),
                @Index(name = "idx_transaction_user_date", columnList = "user_id, transaction_date"),
                @Index(name = "idx_transaction_user_desc_amount_date", columnList = "user_id, description, amount, transaction_date")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "push_notification_id")
    private PushNotification pushNotification;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TransactionType type;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false)
    private TransactionSource source;

    @Column(name = "raw_notification_text", columnDefinition = "TEXT")
    private String rawNotificationText;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Builder
    public Transaction(User user, PushNotification pushNotification, Category category,
                       TransactionType type, Long amount, String description,
                       TransactionSource source, String rawNotificationText, LocalDate transactionDate) {
        this.user = user;
        this.pushNotification = pushNotification;
        this.category = category;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.source = source;
        this.rawNotificationText = rawNotificationText;
        this.transactionDate = transactionDate;
    }

    public void updateCategory(Category category) {
        this.category = category;
    }

    public enum TransactionType {
        INCOME, EXPENSE
    }

    public enum TransactionSource {
        NOTIFICATION, MANUAL
    }
}