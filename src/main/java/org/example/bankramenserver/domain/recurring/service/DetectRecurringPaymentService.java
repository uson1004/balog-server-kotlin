package org.example.bankramenserver.domain.recurring.service;

import lombok.RequiredArgsConstructor;
import org.example.bankramenserver.domain.push.domain.PushNotification;
import org.example.bankramenserver.domain.push.service.SendPushNotificationService;
import org.example.bankramenserver.domain.recurring.domain.RecurringPayment;
import org.example.bankramenserver.domain.recurring.domain.repository.RecurringPaymentRepository;
import org.example.bankramenserver.domain.transaction.domain.Transaction;
import org.example.bankramenserver.domain.transaction.domain.repository.TransactionRepository;
import org.example.bankramenserver.domain.user.domain.User;
import org.example.bankramenserver.domain.user.domain.repository.UserRepository;
import org.example.bankramenserver.global.util.MoneyFormatter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DetectRecurringPaymentService {

    private static final int MONTHLY_TOLERANCE_DAYS = 3;
    private static final int YEARLY_TOLERANCE_DAYS = 7;

    private final TransactionRepository transactionRepository;
    private final RecurringPaymentRepository recurringPaymentRepository;
    private final UserRepository userRepository;
    private final SendPushNotificationService sendPushNotificationService;

    @Transactional
    public void execute(UUID transactionId) {
        Transaction current = transactionRepository.findById(transactionId)
                .orElseThrow();

        if (current.getType() != Transaction.TransactionType.EXPENSE) return;
        if (current.getDescription() == null || current.getDescription().isBlank()) return;

        detectMonthly(current);
        detectYearly(current);
    }

    private void detectMonthly(Transaction current) {
        LocalDate expectedPreviousDate = current.getTransactionDate().minusMonths(1);

        boolean exists = transactionRepository.existsSameExpenseTransactionBetween(
                current.getUser().getId(),
                current.getDescription(),
                current.getAmount(),
                expectedPreviousDate.minusDays(MONTHLY_TOLERANCE_DAYS),
                expectedPreviousDate.plusDays(MONTHLY_TOLERANCE_DAYS)
        );

        if (exists) {
            createCandidateIfNotExists(current, RecurringPayment.Cycle.MONTHLY);
        }
    }

    private void detectYearly(Transaction current) {
        LocalDate expectedPreviousDate = current.getTransactionDate().minusYears(1);

        boolean exists = transactionRepository.existsSameExpenseTransactionBetween(
                current.getUser().getId(),
                current.getDescription(),
                current.getAmount(),
                expectedPreviousDate.minusDays(YEARLY_TOLERANCE_DAYS),
                expectedPreviousDate.plusDays(YEARLY_TOLERANCE_DAYS)
        );

        if (exists) {
            createCandidateIfNotExists(current, RecurringPayment.Cycle.YEARLY);
        }
    }

    private void createCandidateIfNotExists(Transaction current, RecurringPayment.Cycle cycle) {

        boolean alreadyExists = recurringPaymentRepository
                .existsByUser_IdAndNameAndAmountAndCycleAndActiveTrue(
                        current.getUser().getId(),
                        current.getDescription(),
                        current.getAmount(),
                        cycle
                );

        if (alreadyExists) return;

        User user = userRepository.getReferenceById(current.getUser().getId());

        RecurringPayment recurringPayment = RecurringPayment.builder()
                .user(user)
                .category(current.getCategory())
                .name(current.getDescription())
                .amount(current.getAmount())
                .cycle(cycle)
                .billingDay(current.getTransactionDate().getDayOfMonth())
                .nextBillingDate(calculateNextBillingDate(current.getTransactionDate(), cycle))
                .registrationType(RecurringPayment.RegistrationType.AUTO_DETECTED)
                .confirmed(false)
                .build();

        recurringPaymentRepository.save(recurringPayment);

        sendPushNotificationService.execute(
                current.getUser().getId(),
                PushNotification.NotificationType.RECURRING_CANDIDATE,
                "정기결제 같아요",
                "%s %s원이 반복 결제되고 있어요. 정기결제로 등록할까요?"
                        .formatted(
                                current.getDescription(),
                                MoneyFormatter.format(current.getAmount())
                        ),
                recurringPayment.getId().toString(),
                Map.of(
                        "type", PushNotification.NotificationType.RECURRING_CANDIDATE.name(),
                        "recurringPaymentId", recurringPayment.getId().toString()
                )
        );
    }

    private LocalDateTime calculateNextBillingDate(LocalDate transactionDate, RecurringPayment.Cycle cycle) {
        if (cycle == RecurringPayment.Cycle.MONTHLY) {
            return transactionDate.plusMonths(1).atStartOfDay();
        }
        return transactionDate.plusYears(1).atStartOfDay();
    }
}