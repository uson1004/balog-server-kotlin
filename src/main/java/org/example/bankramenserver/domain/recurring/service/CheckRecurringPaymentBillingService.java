package org.example.bankramenserver.domain.recurring.service;

import lombok.RequiredArgsConstructor;
import org.example.bankramenserver.domain.push.domain.PushNotification;
import org.example.bankramenserver.domain.push.service.SendPushNotificationService;
import org.example.bankramenserver.domain.recurring.domain.RecurringPayment;
import org.example.bankramenserver.domain.recurring.domain.RecurringPaymentTransaction;
import org.example.bankramenserver.domain.recurring.domain.repository.RecurringPaymentRepository;
import org.example.bankramenserver.domain.transaction.domain.Transaction;
import org.example.bankramenserver.domain.transaction.domain.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CheckRecurringPaymentBillingService {

    private static final int MONTHLY_TOLERANCE_DAYS = 3;
    private static final int YEARLY_TOLERANCE_DAYS = 7;

    private final RecurringPaymentRepository recurringPaymentRepository;
    private final TransactionRepository transactionRepository;
    private final SendPushNotificationService sendPushNotificationService;
    private final Clock clock;

    @Transactional
    public void execute() {
        LocalDate today = LocalDate.now(clock);

        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        recurringPaymentRepository
                .findAllByActiveTrueAndConfirmedTrueAndNextBillingDateGreaterThanEqualAndNextBillingDateLessThan(
                        start,
                        end
                )
                .forEach(this::check);
    }

    private void check(RecurringPayment recurringPayment) {
        LocalDate expectedDate = recurringPayment.getNextBillingDate().toLocalDate();
        int toleranceDays = getToleranceDays(recurringPayment.getCycle());

        List<Transaction> transactions =
                transactionRepository.findAllByUser_IdAndTypeAndDescriptionAndAmountAndTransactionDateBetweenOrderByTransactionDateDesc(
                        recurringPayment.getUser().getId(),
                        Transaction.TransactionType.EXPENSE,
                        recurringPayment.getName(),
                        recurringPayment.getAmount(),
                        expectedDate.minusDays(toleranceDays),
                        expectedDate.plusDays(toleranceDays)
                );

        if (transactions.isEmpty()) {
            sendMissingNotification(recurringPayment);
            return;
        }

        handlePaid(recurringPayment, transactions.get(0));
    }

    private void handlePaid(RecurringPayment recurringPayment, Transaction transaction) {
        recurringPayment.addTransaction(
                transaction,
                RecurringPaymentTransaction.MatchType.PAYMENT_CONFIRMED,
                LocalDateTime.now(clock)
        );

        sendPaidNotification(recurringPayment);

        recurringPayment.updateAfterPaymentDetected(
                recurringPayment.calculateNextBillingDate()
        );
    }

    private int getToleranceDays(RecurringPayment.Cycle cycle) {
        if (cycle == RecurringPayment.Cycle.MONTHLY) {
            return MONTHLY_TOLERANCE_DAYS;
        }
        return YEARLY_TOLERANCE_DAYS;
    }

    private void sendPaidNotification(RecurringPayment recurringPayment) {
        sendPushNotificationService.execute(
                recurringPayment.getUser().getId(),
                PushNotification.NotificationType.RECURRING_PAYMENT_CONFIRMED,
                "정기결제가 확인됐어요",
                "%s %s원이 정상적으로 결제됐어요."
                        .formatted(recurringPayment.getName(), recurringPayment.getAmount()),
                recurringPayment.getId() + ":PAID:" + recurringPayment.getNextBillingDate().toLocalDate(),
                Map.of(
                        "type", PushNotification.NotificationType.RECURRING_PAYMENT_CONFIRMED.name(),
                        "recurringPaymentId", recurringPayment.getId().toString()
                )
        );
    }

    private void sendMissingNotification(RecurringPayment recurringPayment) {
        sendPushNotificationService.execute(
                recurringPayment.getUser().getId(),
                PushNotification.NotificationType.RECURRING_PAYMENT_MISSING,
                "정기결제 확인이 필요해요",
                "%s %s원 결제가 아직 확인되지 않았어요."
                        .formatted(recurringPayment.getName(), recurringPayment.getAmount()),
                recurringPayment.getId() + ":MISSING:" + recurringPayment.getNextBillingDate().toLocalDate(),
                Map.of(
                        "type", PushNotification.NotificationType.RECURRING_PAYMENT_MISSING.name(),
                        "recurringPaymentId", recurringPayment.getId().toString()
                )
        );
    }
}