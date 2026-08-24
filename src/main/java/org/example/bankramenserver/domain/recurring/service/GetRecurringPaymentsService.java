package org.example.bankramenserver.domain.recurring.service;

import lombok.RequiredArgsConstructor;
import org.example.bankramenserver.domain.recurring.domain.RecurringPayment;
import org.example.bankramenserver.domain.recurring.domain.repository.RecurringPaymentRepository;
import org.example.bankramenserver.domain.recurring.presentation.dto.response.RecurringPaymentListResponse;
import org.example.bankramenserver.domain.recurring.presentation.dto.response.RecurringPaymentResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetRecurringPaymentsService {

    private final RecurringPaymentRepository recurringPaymentRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public RecurringPaymentListResponse execute(UUID userId) {

        List<RecurringPayment> recurringPayments =
                recurringPaymentRepository.findAllByUser_IdAndActiveTrueOrderByNextBillingDateAsc(userId);

        LocalDate today = LocalDate.now(clock);
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate nextMonthStart = monthStart.plusMonths(1);

        Long monthlyScheduledTotalAmount =
                recurringPaymentRepository
                        .findAllByUser_IdAndActiveTrueAndConfirmedTrueAndNextBillingDateBetween(
                                userId,
                                monthStart.atStartOfDay(),
                                nextMonthStart.atStartOfDay()
                        )
                        .stream()
                        .mapToLong(RecurringPayment::getAmount)
                        .sum();
        return new RecurringPaymentListResponse(
                monthlyScheduledTotalAmount,
                recurringPayments.stream()
                        .map(RecurringPaymentResponse::from)
                        .toList()
        );
    }
}