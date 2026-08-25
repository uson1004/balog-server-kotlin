package org.example.bankramenserver.domain.recurring.service

import org.example.bankramenserver.domain.recurring.domain.repository.RecurringPaymentRepository
import org.example.bankramenserver.domain.recurring.exception.RecurringPaymentAccessDeniedException
import org.example.bankramenserver.domain.recurring.exception.RecurringPaymentNotFoundException
import org.example.bankramenserver.domain.recurring.presentation.dto.response.DeleteRecurringPaymentResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class DeleteRecurringPaymentService(private val recurringPaymentRepository: RecurringPaymentRepository) {
    @Transactional
    fun execute(userId: UUID, recurringPaymentId: UUID): DeleteRecurringPaymentResponse {
        val recurringPayment = recurringPaymentRepository.findById(recurringPaymentId).orElseThrow(::RecurringPaymentNotFoundException)
        if (recurringPayment.user.id != userId) throw RecurringPaymentAccessDeniedException()
        recurringPayment.deactivate()
        return DeleteRecurringPaymentResponse(recurringPayment.id!!, recurringPayment.active)
    }
}
