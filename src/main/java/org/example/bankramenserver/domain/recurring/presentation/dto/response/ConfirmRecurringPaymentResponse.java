package org.example.bankramenserver.domain.recurring.presentation.dto.response;

import java.util.UUID;

public record ConfirmRecurringPaymentResponse(
        UUID recurringPaymentId,
        boolean confirmed
) {
}