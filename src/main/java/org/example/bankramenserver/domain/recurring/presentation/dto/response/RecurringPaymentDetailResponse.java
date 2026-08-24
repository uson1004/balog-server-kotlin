package org.example.bankramenserver.domain.recurring.presentation.dto.response;

import org.example.bankramenserver.domain.recurring.domain.RecurringPayment;
import org.example.bankramenserver.domain.recurring.domain.RecurringPaymentTransaction;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public record RecurringPaymentDetailResponse(
        UUID recurringPaymentId,
        String name,
        Long amount,
        String categoryName,
        RecurringPayment.Cycle cycle,
        LocalDate nextBillingDate,
        Integer billingDay,
        RecurringPayment.RegistrationType registrationType,
        boolean confirmed,
        boolean active,
        List<TransactionItem> transactions
) {
    public static RecurringPaymentDetailResponse from(RecurringPayment recurringPayment) {
        return new RecurringPaymentDetailResponse(
                recurringPayment.getId(),
                recurringPayment.getName(),
                recurringPayment.getAmount(),
                recurringPayment.getCategory() != null
                        ? recurringPayment.getCategory().getDisplayName()
                        : null,
                recurringPayment.getCycle(),
                recurringPayment.getNextBillingDate().toLocalDate(),
                recurringPayment.getBillingDay(),
                recurringPayment.getRegistrationType(),
                recurringPayment.isConfirmed(),
                recurringPayment.isActive(),
                recurringPayment.getTransactions()
                        .stream()
                        .sorted(
                                Comparator.comparing(
                                        item -> item.getTransaction().getTransactionDate()
                                )
                        )
                        .map(TransactionItem::from)
                        .toList()
        );
    }

    public record TransactionItem(
            UUID transactionId,
            LocalDate transactionDate,
            String description,
            Long amount,
            RecurringPaymentTransaction.MatchType matchType
    ) {
        public static TransactionItem from(RecurringPaymentTransaction item) {
            return new TransactionItem(
                    item.getTransaction().getId(),
                    item.getTransaction().getTransactionDate(),
                    item.getTransaction().getDescription(),
                    item.getTransaction().getAmount(),
                    item.getMatchType()
            );
        }
    }
}