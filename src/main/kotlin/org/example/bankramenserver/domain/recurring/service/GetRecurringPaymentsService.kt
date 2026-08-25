package org.example.bankramenserver.domain.recurring.service

import org.example.bankramenserver.domain.recurring.domain.repository.RecurringPaymentRepository
import org.example.bankramenserver.domain.recurring.presentation.dto.response.RecurringPaymentListResponse
import org.example.bankramenserver.domain.recurring.presentation.dto.response.RecurringPaymentResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDate
import java.util.UUID

@Service
class GetRecurringPaymentsService(private val recurringPaymentRepository: RecurringPaymentRepository, private val clock: Clock) {
    @Transactional(readOnly = true)
    fun execute(userId: UUID): RecurringPaymentListResponse {
        val recurringPayments = recurringPaymentRepository.findAllByUser_IdAndActiveTrueOrderByNextBillingDateAsc(userId)
        val monthStart = LocalDate.now(clock).withDayOfMonth(1)
        val scheduledAmount = recurringPaymentRepository.findAllByUser_IdAndActiveTrueAndConfirmedTrueAndNextBillingDateBetween(userId, monthStart.atStartOfDay(), monthStart.plusMonths(1).atStartOfDay()).sumOf { it.amount!! }
        return RecurringPaymentListResponse(scheduledAmount, recurringPayments.map(RecurringPaymentResponse::from))
    }
}
