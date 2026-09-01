package org.example.balogserver.domain.transaction.event

import org.example.balogserver.domain.category.domain.Category
import java.time.LocalDate
import java.util.UUID

data class PaymentTransactionRecordedEvent(
    val userId: UUID,
    val transactionId: UUID?,
    val eventId: UUID,
    val title: String?,
    val amount: Long?,
    val category: Category?,
    val occurredAt: LocalDate?,
) {
    fun userId() = userId
    fun transactionId() = transactionId
    fun eventId() = eventId
    fun title() = title
    fun amount() = amount
    fun category() = category
    fun occurredAt() = occurredAt
}
