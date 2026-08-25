package org.example.bankramenserver.domain.recurring.presentation.dto.response

import java.util.UUID
data class DeleteRecurringPaymentResponse(val recurringPaymentId: UUID, val active: Boolean)
