package org.example.bankramenserver.domain.recurring.presentation.dto.response

data class RecurringPaymentListResponse(val monthlyScheduledTotalAmount: Long, val items: List<RecurringPaymentResponse>)
