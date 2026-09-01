package org.example.balogserver.domain.transaction.domain.repository

import org.example.balogserver.domain.transaction.domain.Transaction
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.util.Optional
import java.util.UUID

interface TransactionRepository : JpaRepository<Transaction, UUID>, TransactionRepositoryCustom {
    fun findByIdAndUser_Id(transactionId: UUID?, userId: UUID?): Optional<Transaction>
    fun findAllByUser_IdAndTypeAndDescriptionAndAmountAndTransactionDateBetweenOrderByTransactionDateDesc(userId: UUID?, type: Transaction.TransactionType?, description: String?, amount: Long?, startDate: LocalDate?, endDate: LocalDate?): List<Transaction>
    fun findAllByUser_IdAndTypeAndDescriptionAndAmountOrderByTransactionDateDesc(userId: UUID?, type: Transaction.TransactionType?, description: String?, amount: Long?): List<Transaction>
}
