package org.example.bankramenserver.domain.recurring.presentation.dto.response;

import java.util.List;

public record RecurringPaymentListResponse(
        Long monthlyScheduledTotalAmount,
        List<RecurringPaymentResponse> items
) {
}