package org.example.bankramenserver.domain.recurring.service;

import lombok.RequiredArgsConstructor;
import org.example.bankramenserver.domain.recurring.domain.RecurringPayment;
import org.example.bankramenserver.domain.recurring.domain.repository.RecurringPaymentRepository;
import org.example.bankramenserver.domain.recurring.exception.RecurringPaymentAccessDeniedException;
import org.example.bankramenserver.domain.recurring.exception.RecurringPaymentNotFoundException;
import org.example.bankramenserver.domain.recurring.presentation.dto.response.ConfirmRecurringPaymentResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConfirmRecurringPaymentService {

    private final RecurringPaymentRepository recurringPaymentRepository;

    @Transactional
    public ConfirmRecurringPaymentResponse execute(UUID userId, UUID recurringPaymentId) {
        RecurringPayment recurringPayment = recurringPaymentRepository.findById(recurringPaymentId)
                .orElseThrow(RecurringPaymentNotFoundException::new);

        if (!recurringPayment.getUser().getId().equals(userId)) {
            throw new RecurringPaymentAccessDeniedException();
        }

        recurringPayment.confirm();

        return new ConfirmRecurringPaymentResponse(
                recurringPayment.getId(),
                recurringPayment.isConfirmed()
        );
    }
}