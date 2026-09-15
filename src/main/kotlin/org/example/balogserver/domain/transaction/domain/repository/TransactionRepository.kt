package org.example.balogserver.domain.transaction.domain.repository

import org.example.balogserver.domain.transaction.domain.Transaction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.Optional
import java.util.UUID

interface TransactionRepository : JpaRepository<Transaction, UUID>, TransactionRepositoryCustom {
    fun findByIdAndUser_Id(transactionId: UUID?, userId: UUID?): Optional<Transaction>
    fun findByUser_IdAndIdempotencyKey(userId: UUID, idempotencyKey: String): Optional<Transaction>
    fun findTop50BySourceAndCategorySourceOrderByCreatedAtAsc(source: Transaction.TransactionSource, categorySource: Transaction.CategorySource): List<Transaction>
    @Modifying
    @Transactional
    @Query("update Transaction t set t.category = :category, t.categorySource = :classifiedSource where t.id = :transactionId and t.categorySource = :pendingSource")
    fun updateNotificationCategoryIfPending(@Param("transactionId") transactionId: UUID, @Param("category") category: org.example.balogserver.domain.category.domain.Category, @Param("pendingSource") pendingSource: Transaction.CategorySource, @Param("classifiedSource") classifiedSource: Transaction.CategorySource): Int
    fun findAllByUser_IdAndTypeAndDescriptionAndAmountAndTransactionDateBetweenOrderByTransactionDateDesc(userId: UUID?, type: Transaction.TransactionType?, description: String?, amount: Long?, startDate: LocalDate?, endDate: LocalDate?): List<Transaction>
    fun findAllByUser_IdAndTypeAndDescriptionAndAmountOrderByTransactionDateDesc(userId: UUID?, type: Transaction.TransactionType?, description: String?, amount: Long?): List<Transaction>
}
