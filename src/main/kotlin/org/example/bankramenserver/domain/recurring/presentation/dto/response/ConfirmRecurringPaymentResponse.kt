package org.example.bankramenserver.domain.recurring.presentation.dto.response

import java.util.UUID
data class ConfirmRecurringPaymentResponse(val recurringPaymentId: UUID, val confirmed: Boolean)
