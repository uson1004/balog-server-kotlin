package org.example.balogserver.domain.transaction.domain.repository

import org.example.balogserver.domain.category.domain.Category
import org.example.balogserver.domain.transaction.domain.Transaction
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class TransactionHistoryRow(
    private val transactionIdValue: UUID?,
    private val titleValue: String?,
    private val transactionDateValue: LocalDate?,
    private val createdAtValue: LocalDateTime?,
    private val amountValue: Long?,
    private val typeValue: Transaction.TransactionType?,
    private val categoryValue: Category?,
) {
    val transactionId get() = transactionIdValue
    val title get() = titleValue
    val transactionDate get() = transactionDateValue
    val createdAt get() = createdAtValue
    val amount get() = amountValue
    val type get() = typeValue
    val category get() = categoryValue!!
    fun transactionId() = transactionIdValue
    fun title() = titleValue
    fun transactionDate() = transactionDateValue
    fun createdAt() = createdAtValue
    fun amount() = amountValue
    fun type() = typeValue
    fun category() = categoryValue

    companion object { @JvmStatic fun builder() = TransactionHistoryRowBuilder() }
    class TransactionHistoryRowBuilder {
        private var transactionId: UUID? = null
        private var title: String? = null
        private var transactionDate: LocalDate? = null
        private var createdAt: LocalDateTime? = null
        private var amount: Long? = null
        private var type: Transaction.TransactionType? = null
        private var category: Category? = null
        fun transactionId(value: UUID?) = apply { transactionId = value }
        fun title(value: String?) = apply { title = value }
        fun transactionDate(value: LocalDate?) = apply { transactionDate = value }
        fun createdAt(value: LocalDateTime?) = apply { createdAt = value }
        fun amount(value: Long?) = apply { amount = value }
        fun type(value: Transaction.TransactionType?) = apply { type = value }
        fun category(value: Category?) = apply { category = value }
        fun build() = TransactionHistoryRow(transactionId, title, transactionDate, createdAt, amount, type, category)
    }
}
