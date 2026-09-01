package org.example.balogserver.domain.recurring.presentation.dto.response

data class RecurringPaymentListResponse(val monthlyScheduledTotalAmount: Long, val items: List<RecurringPaymentResponse>)
