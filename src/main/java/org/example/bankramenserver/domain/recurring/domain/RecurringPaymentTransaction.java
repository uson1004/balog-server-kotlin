package org.example.bankramenserver.domain.recurring.domain;

import jakarta.persistence.*;
import lombok.*;
import org.example.bankramenserver.domain.transaction.domain.Transaction;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(
        name = "recurring_payment_transactions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_recurring_payment_transaction",
                        columnNames = {"recurring_payment_id", "transaction_id"}
                )
        }
)
public class RecurringPaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "recurring_payment_transaction_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recurring_payment_id", nullable = false)
    private RecurringPayment recurringPayment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchType matchType;

    @Column(nullable = false)
    private LocalDateTime matchedAt;

    public enum MatchType {
        INITIAL,
        AUTO_DETECTED,
        MANUAL_ADDED,
        PAYMENT_CONFIRMED
    }
}