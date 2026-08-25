package org.example.bankramenserver.domain.transaction.domain.repository

import org.example.bankramenserver.domain.transaction.domain.Transaction
import java.time.LocalDate
import java.util.UUID

interface TransactionRepositoryCustom {
    fun findTransactionHistories(userId: UUID?, transactionType: Transaction.TransactionType?, startDate: LocalDate?, endDate: LocalDate?): List<TransactionHistoryRow>
    fun findRecentTransactionHistories(userId: UUID?, limit: Int): List<TransactionHistoryRow>
    fun findUserIdsHavingTransactionsBetween(startDate: LocalDate?, endDate: LocalDate?): List<UUID>
    fun existsSameExpenseTransactionBetween(userId: UUID?, description: String?, amount: Long?, startDate: LocalDate?, endDate: LocalDate?): Boolean
}
