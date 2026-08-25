package org.example.bankramenserver.domain.recurring.presentation.dto.response

import org.example.bankramenserver.domain.recurring.domain.RecurringPayment
import java.time.LocalDate
import java.util.UUID

data class RecurringPaymentResponse(
    val recurringPaymentId: UUID, val name: String, val amount: Long, val category: String?, val categoryDisplayName: String?,
    val cycle: RecurringPayment.Cycle, val billingDay: Int, val nextBillingDate: LocalDate, val registrationType: RecurringPayment.RegistrationType,
    val confirmed: Boolean, val transactionCount: Int, val lastPaidDate: LocalDate?,
) {
    companion object {
        @JvmStatic
        fun from(recurringPayment: RecurringPayment): RecurringPaymentResponse {
            val category = recurringPayment.category
            return RecurringPaymentResponse(
                recurringPayment.id!!, recurringPayment.name, recurringPayment.amount!!, category?.name, category?.displayName,
                recurringPayment.cycle, recurringPayment.billingDay, recurringPayment.nextBillingDate.toLocalDate(), recurringPayment.registrationType,
                recurringPayment.confirmed, recurringPayment.transactions.size, recurringPayment.transactions.maxOfOrNull { it.transaction.transactionDate },
            )
        }
    }
}
