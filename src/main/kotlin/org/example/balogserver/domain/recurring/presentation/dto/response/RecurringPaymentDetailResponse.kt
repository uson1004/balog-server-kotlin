package org.example.balogserver.domain.recurring.presentation.dto.response

import org.example.balogserver.domain.recurring.domain.RecurringPayment
import org.example.balogserver.domain.recurring.domain.RecurringPaymentTransaction
import java.time.LocalDate
import java.util.UUID

data class RecurringPaymentDetailResponse(
    val recurringPaymentId: UUID, val name: String, val amount: Long, val categoryName: String?, val cycle: RecurringPayment.Cycle,
    val nextBillingDate: LocalDate, val billingDay: Int, val registrationType: RecurringPayment.RegistrationType, val confirmed: Boolean,
    val active: Boolean, val transactions: List<TransactionItem>,
) {
    companion object {
        @JvmStatic
        fun from(recurringPayment: RecurringPayment) = RecurringPaymentDetailResponse(
            recurringPayment.id!!, recurringPayment.name, recurringPayment.amount!!, recurringPayment.category?.displayName,
            recurringPayment.cycle, recurringPayment.nextBillingDate.toLocalDate(), recurringPayment.billingDay,
            recurringPayment.registrationType, recurringPayment.confirmed, recurringPayment.active,
            recurringPayment.transactions.sortedBy { it.transaction.transactionDate }.map { TransactionItem.from(it) },
        )
    }
    data class TransactionItem(val transactionId: UUID, val transactionDate: LocalDate, val description: String?, val amount: Long, val matchType: RecurringPaymentTransaction.MatchType) {
        companion object {
            @JvmStatic fun from(item: RecurringPaymentTransaction) = TransactionItem(item.transaction.id!!, item.transaction.transactionDate, item.transaction.description!!, item.transaction.amount!!, item.matchType)
        }
    }
}
