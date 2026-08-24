package org.example.bankramenserver.domain.recurring.service;

import lombok.RequiredArgsConstructor;
import org.example.bankramenserver.domain.push.domain.PushNotification;
import org.example.bankramenserver.domain.push.service.SendPushNotificationService;
import org.example.bankramenserver.domain.recurring.domain.RecurringPayment;
import org.example.bankramenserver.domain.recurring.domain.repository.RecurringPaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.NumberFormat;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SendRecurringPaymentReminderService {

    private final RecurringPaymentRepository recurringPaymentRepository;
    private final SendPushNotificationService sendPushNotificationService;
    private final Clock clock;

    @Transactional
    public void execute() {
        LocalDate tomorrow = LocalDate.now(clock).plusDays(1);

        recurringPaymentRepository.findAllByActiveTrueAndConfirmedTrueAndNextBillingDateBetween(
                tomorrow.atStartOfDay(),
                tomorrow.plusDays(1).atStartOfDay()
        ).forEach(this::sendReminder);
    }

    private void sendReminder(RecurringPayment recurringPayment) {
        String formattedAmount = NumberFormat.getNumberInstance(Locale.KOREA)
                .format(recurringPayment.getAmount());

        sendPushNotificationService.execute(
                recurringPayment.getUser().getId(),
                PushNotification.NotificationType.RECURRING_PAYMENT_REMINDER,
                "내일 정기결제 예정이에요",
                "%s %s원이 내일 결제될 예정이에요."
                        .formatted(recurringPayment.getName(), formattedAmount),
                recurringPayment.getId() + ":REMINDER:" + recurringPayment.getNextBillingDate().toLocalDate(),
                Map.of(
                        "type", PushNotification.NotificationType.RECURRING_PAYMENT_REMINDER.name(),
                        "recurringPaymentId", recurringPayment.getId().toString(),
                        "billingDate", recurringPayment.getNextBillingDate().toLocalDate().toString()
                )
        );
    }
}